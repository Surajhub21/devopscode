# My Last CI/CD Automation Project — Jenkins + Maven + Dockerized Tomcat

🤩 Introducing a fully automated CI/CD pipeline: every GitHub commit triggers an automated build and test run — if tests fail, the pipeline sends an email alert; if they pass, the app is packaged and deployed to a Tomcat server, leaving you with a live, running application. Built with Jenkins + Maven, using Dockerized Tomcat instances and SSH for secure transfer, this pipeline speeds delivery and removes manual deployment steps.

---

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quickstart](#quickstart)
- [Jenkins Pipeline Stages](#jenkins-pipeline-stages)
- [Notifications & Error Handling](#notifications--error-handling)
- [Security & Best Practices](#security--best-practices)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

## Overview
This project demonstrates a production-like CI/CD pipeline using Jenkins as the orchestrator and Maven as the build tool. Commits to the repository trigger the pipeline which:
1. Checks out code
2. Builds and runs unit tests with Maven
3. If tests pass: builds the artifact (WAR), transfers it to a Tomcat server (Dockerized) via SSH, and deploys it
4. If tests fail: sends email alerts to the configured recipients

The goal is to remove manual deployment steps, speed delivery, and provide fast feedback on failing changes.

---

## Architecture
- GitHub: Source repository & webhooks trigger Jenkins
- Jenkins: CI/CD orchestration (Jenkinsfile pipeline)
- Maven: Build lifecycle, dependency management, and test execution
- Docker: Tomcat server runs inside Docker for isolation and reproducibility
- SSH (scp/ssh): Secure transfer of build artifacts and remote deploy commands
- Email: SMTP-based notifications for failed builds/tests

ASCII flow:
GitHub (push) --> Jenkins (checkout, build/test) --> [fail] Email alert
                                          \
                                           --> [pass] scp WAR --> Dockerized Tomcat (restart/deploy)

---

## Features
- Automated builds on every Git push
- Maven-driven build and test execution
- Email notifications on failures
- Automated packaging and deployment to a remote Tomcat container
- Dockerized Tomcat instances for predictable runtime
- Secure artifact transfer using SSH
- Clear, stage-based Jenkins pipeline (build → test → package → deploy)

---

## Prerequisites
- Jenkins (LTS recommended) with:
  - Git plugin
  - Pipeline plugin
  - SSH Agent / Publish Over SSH plugin (or configured SSH in pipeline)
  - Email Extension plugin (or SMTP configured)
- Maven installed on the Jenkins agent (or use a Maven Docker image)
- Docker on the deployment host (Tomcat runs in a container)
- SSH access from Jenkins to the deployment host (SSH key with restricted access)
- GitHub webhook configured to notify Jenkins of push events
- SMTP credentials for notification emails

---

## Quickstart

1. Clone the repository:
```bash
git clone https://github.com/<owner>/<repo>.git
cd <repo>
```

2. Configure Jenkins:
- Create a new Pipeline job (or Multibranch Pipeline).
- Point the job to this repository.
- If using multibranch, Jenkins will discover branches automatically.
- Add necessary credentials:
  - SSH private key (id: `deploy-ssh-key`) for deployment host
  - SMTP/email credentials (if required)

3. Add a GitHub webhook to your repo:
- Payload URL: <JENKINS_URL>/github-webhook/
- Content type: application/json
- Events: push (and PR events if desired)

4. Prepare your deployment host:
- Install Docker
- Ensure Jenkins' SSH user can scp/ssh to the host
- (Optional) Create a Docker image or use an official Tomcat image
  Example (run Tomcat):
  ```bash
  docker run -d --name app-tomcat -p 8080:8080 tomcat:9.0
  ```

5. Commit code and push. Jenkins will pick it up and run the pipeline.

---

## Jenkins Pipeline Stages
Typical Jenkinsfile (high-level overview):

```groovy
pipeline {
  agent any
  environment {
    DEPLOY_HOST = 'deploy.example.com'
    DEPLOY_USER = 'jenkins-deploy'
    SSH_CRED_ID = 'deploy-ssh-key'
    EMAIL_RECIPIENTS = 'dev-team@example.com'
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build') {
      steps { sh 'mvn -B clean package' }
    }
    stage('Test') {
      steps { sh 'mvn -B test' }
    }
    stage('Package') {
      steps {
        archiveArtifacts artifacts: 'target/*.war', fingerprint: true
      }
    }
    stage('Deploy') {
      when { expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' } }
      steps {
        // Use SSH agent and scp to transfer artifact, then remote docker restart or copy into container
        sshagent (credentials: [env.SSH_CRED_ID]) {
          sh """
            scp target/myapp.war ${DEPLOY_USER}@${DEPLOY_HOST}:/tmp/
            ssh ${DEPLOY_USER}@${DEPLOY_HOST} 'docker cp /tmp/myapp.war app-tomcat:/usr/local/tomcat/webapps/ && docker restart app-tomcat'
          """
        }
      }
    }
  }
  post {
    failure {
      mail to: "${EMAIL_RECIPIENTS}",
           subject: "Build failed in ${env.JOB_NAME} #${env.BUILD_NUMBER}",
           body: "Build failed. Check Jenkins: ${env.BUILD_URL}"
    }
    success {
      echo "Deployment successful — app should be live."
    }
  }
}
```

Adjust paths, image names, and commands to match your app and environment.

---

## Notifications & Error Handling
- Tests failing: pipeline halts; post/failure block sends an email with build link and logs.
- Deployment errors: captured in Jenkins console; optional retry steps can be implemented.
- Consider Slack or other chat integrations for team visibility.

---

## Security & Best Practices
- Use Jenkins Credentials store for SSH keys and SMTP credentials; never hard-code secrets.
- Limit SSH key permissions and use a dedicated deployment user on the host.
- Run Tomcat inside a non-privileged container and avoid exposing management ports publicly.
- Keep images and OS packages up to date.
- Use branch protections and PR reviews to reduce the chance of broken pipelines on main branches.
- Enable pipeline durability/agent isolation to reduce cross-job interference.

---

## Troubleshooting
- Build fails locally but passes in Jenkins:
  - Check environment differences: Java/Maven versions, env vars.
- Jenkins cannot SSH to host:
  - Verify key is installed in deploy user's `~/.ssh/authorized_keys`.
  - Ensure host firewall allows SSH from Jenkins.
- Tomcat not serving new WAR:
  - Confirm docker cp path and Tomcat auto-deploy settings; consider replacing container rather than copying into it.

---

## Contributing
Contributions are welcome. If you want to extend this pipeline (add canary deploys, blue-green, feature flags, etc.), open a PR with:
- A clear description of changes
- Updated Jenkinsfile or scripts
- Any new documentation or runbooks

---

## License
This project is provided as-is. Add a LICENSE file for your chosen license.

---

## Contact
Maintainer: Surajhub21
For questions or help with this pipeline, open an issue or reach out via GitHub.

---

Enjoy faster, safer releases — and let automation do the heavy lifting!
