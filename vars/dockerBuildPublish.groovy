// vars/dockerBuildPublish.groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    //tagAsLatest defaults to latest
    // github-organization-plugin jobs are named as 'org/repo/branch'
    tokens = "${env.JOB_NAME}".tokenize('/')
    org = tokens[0]
    repo = tokens[1]
    branch = tokens[2]
    def tagAsLatest = config.tagAsLatest ?: true
    def dockerUserOrg = config.dockerUserOrg ?: org
    def dockerRepoName = config.dockerRepoName ?: repo
    def dockerTag = config.dockerTag ?: branch

    def dockerImage
    node('docker-cloud') {
      stage 'Build Docker Image'
        checkout scm
        dockerImage = docker.build "${dockerUserOrg}/${dockerRepoName}:${dockerTag}"
    
      stage 'Publish Docker Image'
          withDockerRegistry(registry: [credentialsId: 'docker-hub-kb']) {
            dockerImage.push()
            if(tagAsLatest) {
              dockerImage.push("${dockerUserOrg}/${dockerRepoName}:latest")
            }
          }
    }
}
