java -Xmx512M -Xss2M -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch-0.11.3.jar "$@"
