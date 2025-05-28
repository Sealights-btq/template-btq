pipeline {
   agent {
        kubernetes {
            defaultContainer 'shell'
            yaml """
                apiVersion: v1
                kind: Pod
                metadata:
                  labels:
                    some-label: some-value
                spec:
                  containers:
                  - name: shell
                    image: "public.ecr.aws/a2q7i5i2/sl-jenkins-base-ci:latest"
                    command:
                    - cat
                    tty: true
                    env:
                      - name: AWS_ACCESS_KEY_ID
                        valueFrom:
                          secretKeyRef:
                            name: aws-secret
                            key: AWS_ACCESS_KEY_ID
                      - name: AWS_SECRET_ACCESS_KEY
                        valueFrom:
                          secretKeyRef:
                            name: aws-secret
                            key: AWS_SECRET_ACCESS_KEY
            """
        }
    }
    environment {
        IDENTIFIER = 'template.btq.sealights.co'
    }
     stages {
        stage("Uninstalling helm") {
            steps {
                script {
                    withCredentials([string(credentialsId: 'ssh-key', variable: 'SSH_KEY')]) {
                        sh script: """
                            echo '${SSH_KEY}' > key.pem
                            chmod 0400 key.pem

                            ssh -o StrictHostKeyChecking=no -i key.pem ec2-user@template.btq.sealights.co 'export KUBECONFIG=\$(k3d kubeconfig write btq) && helm uninstall btq'
                        """
                    }
                }
            }
        }
    }
}
