properties([
    disableConcurrentBuilds()
])
if (env.BRANCH_NAME == 'master') {
    properties([
    ])
}
pipeline {
    agent { label "devel8" }
    tools {
        maven "maven 3.5"
    }
    environment {
        MAVEN_OPTS = "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
    }
    triggers {
        pollSCM("H/3 * * * *")
    }
    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: "", artifactNumToKeepStr: "", daysToKeepStr: "30", numToKeepStr: "30"))
        timestamps()
    }
    stages {
        stage("build") {
            steps {
                // Fail Early..
                script {
                    if (! env.BRANCH_NAME) {
                        currentBuild.result = Result.ABORTED
                        throw new hudson.AbortException('Job Started from non MultiBranch Build')
                    } else {
                        println(" Building BRANCH_NAME == ${BRANCH_NAME}")
                    }

                    def status = sh returnStatus: true, script:  """
                        rm -rf \$WORKSPACE/.repo
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo dependency:resolve dependency:resolve-plugins >/dev/null
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo clean
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo --fail-at-end clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dsurefire.useFile=false
                    """

                    // We want code-coverage and pmd/findbugs even if unittests fails
                    status += sh returnStatus: true, script:  """
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo pmd:pmd pmd:cpd findbugs:findbugs javadoc:aggregate
                    """

                    junit testResults: '**/target/surefire-reports/TEST-*.xml'

                    def java = scanForIssues tool: [$class: 'Java']
                    def javadoc = scanForIssues tool: [$class: 'JavaDoc']
                    publishIssues issues:[java, javadoc], unstableTotalAll:1

                    def pmd = scanForIssues tool: [$class: 'Pmd'], pattern: '**/target/pmd.xml'
                    publishIssues issues:[pmd], unstableTotalAll:1

                    def cpd = scanForIssues tool: [$class: 'Cpd'], pattern: '**/target/cpd.xml'
                    publishIssues issues:[cpd]

                    def findbugs = scanForIssues tool: [$class: 'FindBugs'], pattern: '**/target/findbugsXml.xml'
                    publishIssues issues:[findbugs], unstableTotalAll:1

                    step([$class: 'JacocoPublisher', 
                          execPattern: '**/target/*.exec',
                          classPattern: '**/target/classes',
                          sourcePattern: '**/src/main/java',
                          exclusionPattern: '**/src/test*'
                    ])

                    if ( status != 0 ) {
                        currentBuild.result = Result.FAILURE
                    }
                }
            } 
        }

        stage("coverage") {
            steps {
                step([$class: 'JacocoPublisher', 
                      execPattern: '**/target/*.exec',
                      classPattern: '**/target/classes',
                      sourcePattern: '**/src/main/java',
                      exclusionPattern: '**/src/test*'
                ])
            }
        }

        stage("docker") {
            steps {
                script {
                    def allDockerFiles = findFiles glob: '**/Dockerfile'
                    def dockerFiles = allDockerFiles.findAll { f -> f.path.endsWith("target/docker/Dockerfile") }
                    def version = readMavenPom().version

                    for (def f : dockerFiles) {
                        def dirName = f.path.take(f.path.length() - "target/docker/Dockerfile".length())
                        if ( dirName == '' )
                            dirName = '.'
                        dir(dirName) {
                            modulePom = readMavenPom file: 'pom.xml'
                            def projectArtifactId = modulePom.getArtifactId()
                            def imageName = "${projectArtifactId}-${version}".toLowerCase()
                            if (! env.CHANGE_BRANCH) {
                                imageLabel = env.BRANCH_NAME.toLowerCase()
                            } else {
                                imageLabel = env.CHANGE_BRANCH.toLowerCase()
                            }
                            if ( ! (imageLabel ==~ /master|trunk/) ) {
                                println("Using branch_name ${imageLabel}")
                                imageLabel = imageLabel.split(/\//)[-1]
                            } else {
                                println(" Using Master branch ${BRANCH_NAME}")
                                imageLabel = env.BUILD_NUMBER
                            }

                            println("In ${dirName} build ${projectArtifactId} as ${imageName}:$imageLabel")

                            def app = docker.build("$imageName:${imageLabel}", '--pull --no-cache --file target/docker/Dockerfile .')

                            if (currentBuild.resultIsBetterOrEqualTo(Result.SUCCESS)) {
                                docker.withRegistry('https://docker-os.dbc.dk', 'docker') {
                                    app.push()
                                    if (env.BRANCH_NAME ==~ /master|trunk/) {
                                        app.push "latest"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
