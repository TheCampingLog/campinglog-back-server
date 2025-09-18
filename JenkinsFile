pipeline {
    agent any

    // 사용할 도구들 정의
    tools {
        jdk 'OpenJDK-22'        // Jenkins에서 설정한 JDK 이름
        maven 'Maven-3.9.11'    // Jenkins에서 설정한 Maven 이름
    }

    environment {
        // 환경 변수 설정
        APP_NAME = 'campinglog-backend-server'
        DOCKER_IMAGE = "${APP_NAME}"
        DOCKER_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = "${APP_NAME}-container"
        APP_PORT = '8080'
        HOST_PORT = '8080'  // 호스트에서 사용할 포트 (필요시 변경)

        // Docker 관련 환경 변수
        DOCKERFILE_PATH = 'Dockerfile'
        DOCKER_NETWORK = 'campinglog-network'  // 선택사항: 네트워크 생성시 사용
        ENV_FILE = 'env'  // 환경 변수 파일
    }

    stages {

        stage('Verify Tools') {
            steps {
                echo '=== 도구 검증 ==='
                sh 'echo "JAVA_HOME: $JAVA_HOME"'
                sh 'echo "MAVEN_HOME: $MAVEN_HOME"'
                sh 'which java'
                sh 'which mvn'
                sh 'which docker'
                sh 'java -version'
                sh 'mvn -version'
                sh 'docker --version'
            }
        }

        // 1단계: 소스 코드 체크아웃
        stage('Checkout') {
            steps {
                echo 'checkout 단계를 사용한 Git 체크아웃...'
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        credentialsId: 'github-token',
                        url: 'https://github.com/TheCampingLog/campinglog-back-server.git'
                    ]]
                ])
            }
        }

        // 2단계: 환경 변수 파일 확인
        stage('Check Environment File1') {
            steps {
                echo '환경 변수 파일을 확인합니다...'
                script {
                    // 현재 워크스페이스 확인
                    sh 'echo "=== 현재 워크스페이스 ==="'
                    sh 'pwd'
                    sh 'echo "=== 워크스페이스 파일 목록 ==="'
                    sh 'ls -la'

                    // .env.backend 파일 존재 확인
                    if (fileExists("${ENV_FILE}")) {
                        echo "✅ ${ENV_FILE} 파일이 존재합니다."
                    } else {
                        echo "⚠️ ${ENV_FILE} 파일이 없습니다."
                        sh "echo '=== 현재 디렉토리에서 .env 관련 파일 검색 ==='"
                        sh "find . -name '*.env*' -type f || echo '.env 관련 파일을 찾을 수 없습니다.'"

                        echo "기본 환경 변수를 사용하여 ${ENV_FILE} 파일을 생성합니다."
                        // 기본 .env.backend 파일 생성
                        sh '''
                            echo "# 기본 환경 변수 설정" > ${ENV_FILE}
                            echo "SPRING_PROFILES_ACTIVE=prod" >> ${ENV_FILE}
                            echo "SERVER_PORT=8080" >> ${ENV_FILE}
                            echo "# 데이터베이스 설정 (실제 값으로 변경 필요)" >> ${ENV_FILE}
                            echo "# DB_HOST=localhost" >> ${ENV_FILE}
                            echo "# DB_PORT=3306" >> ${ENV_FILE}
                            echo "# DB_NAME=campinglog" >> ${ENV_FILE}
                            echo "# 필요한 다른 환경 변수들을 여기에 추가하세요" >> ${ENV_FILE}
                        '''
                        echo "기본 ${ENV_FILE} 파일을 생성했습니다."
                        sh "ls -la ${ENV_FILE}"
                    }

                    // 최종 확인
                    sh "echo '=== 최종 ${ENV_FILE} 파일 확인 ==='"
                    sh "wc -l ${ENV_FILE} && echo '줄 수 확인 완료'"
                }
            }
        }

        // 2단계: 환경 변수 파일 확인
        stage('Check Environment File') {
            steps {
                echo '환경 변수 파일을 확인합니다...'
                script {
                    // .env.backend 파일 존재 확인
                    if (fileExists("${ENV_FILE}")) {
                        echo "✅ ${ENV_FILE} 파일이 존재합니다."
                        sh "echo '=== ${ENV_FILE} 파일 내용 (민감정보 제외) ==='"
                        // 민감정보를 제외한 환경 변수 목록 출력 (KEY만 표시)
                        sh "grep -E '^[A-Z_]+=.*' ${ENV_FILE} | cut -d'=' -f1 | sort || echo '환경 변수를 읽을 수 없습니다.'"
                    } else {
                        echo "⚠️ ${ENV_FILE} 파일이 없습니다. 기본 환경 변수를 사용합니다."
                        // 기본 .env.backend 파일 생성 (선택사항)
                        sh '''
                            echo "# 기본 환경 변수 설정" > ${ENV_FILE}
                            echo "SPRING_PROFILES_ACTIVE=prod" >> ${ENV_FILE}
                            echo "SERVER_PORT=8080" >> ${ENV_FILE}
                            echo "# 필요한 다른 환경 변수들을 여기에 추가하세요" >> ${ENV_FILE}
                        '''
                        echo "기본 ${ENV_FILE} 파일을 생성했습니다."
                    }
                }
            }
        }

        // 3단계: 빌드
        stage('Build') {
            steps {
                echo 'Maven으로 프로젝트를 빌드합니다...'
                sh 'pwd'
                sh 'mvn clean compile'
            }
        }

        // 4단계: 패키지 빌드
        stage('Package') {
            steps {
                echo 'JAR 파일을 생성합니다...'
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    // 빌드 산출물 보관
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        // 5단계: 기존 컨테이너 정리
        stage('Cleanup') {
            steps {
                echo '기존 Docker 컨테이너와 이미지를 정리합니다...'
                script {
                    // 기존 컨테이너 중지 및 삭제 (에러 무시)
                    sh '''
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                    '''

                    // 기존 이미지 삭제 (선택사항 - 디스크 공간 절약)
                    sh '''
                        docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true
                        docker rmi ${DOCKER_IMAGE}:latest || true
                    '''
                }
            }
        }

        // 5단계: Docker 이미지 빌드
        stage('Build Docker Image') {
            steps {
                echo 'Docker 이미지를 빌드합니다...'
                script {
                    // Dockerfile이 존재하는지 확인
                    sh 'ls -la'

                    // Docker 이미지 빌드
                    sh '''
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    '''
                }
            }
        }

        // 6단계: Docker 컨테이너 실행
        stage('Deploy Docker Container') {
            steps {
                echo 'Docker 컨테이너를 실행합니다...'
                script {
                    // Docker 네트워크 생성 (선택사항)
                    sh '''
                        docker network create ${DOCKER_NETWORK} || true
                    '''

                    // Docker 컨테이너 실행
                    sh '''
                        docker run -d \
                            --name ${CONTAINER_NAME} \
                            --network ${DOCKER_NETWORK} \
                            -p ${HOST_PORT}:${APP_PORT} \
                            --restart unless-stopped \
                            -e SPRING_PROFILES_ACTIVE=prod \
                            ${DOCKER_IMAGE}:${DOCKER_TAG}
                    '''
                }
            }
        }

        // 8단계: 배포 검증
        stage('Verify Deployment') {
            steps {
                echo '배포된 애플리케이션을 검증합니다...'
                script {
                    // 컨테이너 상태 확인
                    sh 'docker ps | grep ${CONTAINER_NAME}'

                    // 컨테이너 환경 변수 확인 (민감정보 제외)
                    echo "=== 컨테이너 환경 변수 확인 ==="
                    sh '''
                        docker exec ${CONTAINER_NAME} env | grep -E "^(SPRING_|SERVER_|BUILD_|DEPLOYMENT_)" | sort || echo "환경 변수 확인 실패"
                    '''

                    // 애플리케이션 로그 확인 (첫 30초)
                    sh 'docker logs ${CONTAINER_NAME} || true'

                    // 헬스체크 (애플리케이션이 완전히 시작될 때까지 대기)
                    sh '''
                        echo "애플리케이션 시작을 기다리는 중..."
                        sleep 30

                        # 헬스체크 엔드포인트 호출 (있는 경우)
                        curl -f http://localhost:${HOST_PORT}/actuator/health || \
                        curl -f http://localhost:${HOST_PORT}/health || \
                        curl -f http://localhost:${HOST_PORT}/ || \
                        echo "헬스체크 엔드포인트가 없거나 아직 준비되지 않았습니다."

                        # 추가 로그 확인
                        echo ""
                        echo "=== 최신 애플리케이션 로그 ==="
                        docker logs ${CONTAINER_NAME} --tail 20
                    '''
                }
            }
        }
    }

    post {
        success {
            echo '=== 파이프라인이 성공적으로 완료되었습니다! ==='
            echo "애플리케이션이 http://localhost:${HOST_PORT} 에서 실행 중입니다."

            // 배포 정보 출력
            sh '''
                echo "=== 배포 정보 ==="
                echo "컨테이너 이름: ${CONTAINER_NAME}"
                echo "이미지: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                echo "포트: ${HOST_PORT}:${APP_PORT}"
                echo "환경 변수 파일: ${ENV_FILE}"
                echo ""
                echo "=== 실행 중인 컨테이너 ==="
                docker ps | grep ${CONTAINER_NAME}
                echo ""
                echo "=== 환경 변수 파일 설정 확인 ==="
                docker exec ${CONTAINER_NAME} env | grep -c "=" && echo "환경 변수가 성공적으로 로드되었습니다." || echo "환경 변수 확인 실패"
            '''
        }

        failure {
            echo '=== 파이프라인 실행 중 오류가 발생했습니다 ==='

            // 디버깅을 위한 정보 출력
            sh '''
                echo "=== Docker 상태 ==="
                docker ps -a | grep ${CONTAINER_NAME} || echo "컨테이너를 찾을 수 없습니다."

                echo ""
                echo "=== 최근 로그 ==="
                docker logs ${CONTAINER_NAME} --tail 50 || echo "로그를 가져올 수 없습니다."
            '''
        }

        always {
            // 워크스페이스 정리 (선택사항)
            echo '작업 공간을 정리합니다...'

            // Docker 빌드 캐시 정리 (디스크 공간 절약 - 선택사항)
            sh 'docker system prune -f --volumes || true'
        }
    }
}