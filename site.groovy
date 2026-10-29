
pipeline {
    agent { label 'main' }

    stages {
        stage('Cleanws') {
            steps {
                cleanWs()
            }
        }

        stage('Update Ubuntu') {
            steps {
                sshagent(['docker_host']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${hostname} \
                        "sudo apt update -y"
                    '''
                }
            }
        }

        stage('Install Apache and Git') {
            steps {
                sshagent(['docker_host']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${hostname} \
                        "sudo apt install -y apache2 git rsync"
                    '''
                }
            }
        }

        stage('Clone Git Repository') {
            steps {
                sshagent(['docker_host']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${hostname} \
                        "rm -rf /tmp/site && \
                        git clone https://github.com/shivakumar-dheenabhandu/test.git /tmp/site"
                    '''
                }
            }
        }

        stage('Deploy Website') {
            steps {
                sshagent(['docker_host']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${hostname} \
                        "sudo rsync -av --delete --exclude=.git \
                        /tmp/site/ /var/www/html/"
                    '''
                }
            }
        }

        stage('Restart Apache') {
            steps {
                sshagent(['docker_host']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${hostname} \
                        "sudo systemctl enable apache2 && \
                        sudo systemctl restart apache2"
                    '''
                }
            }
        }
    }
}
