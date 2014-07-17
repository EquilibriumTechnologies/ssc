bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

#conf stuff to pull out to its own file?
HOSTNAME=`hostname`
SSC_JVM_OPTIONS="-Djsse.enableSNIExtension=false "

echo "libs at: $bin/../lib"

for x in $bin/../lib/*.jar; do
  SSC_CLASSPATH=$x:$SSC_CLASSPATH
done

SSC_CLASSPATH=$bin/../conf:$SSC_CLASSPATH

export SSC_HOME="$bin"/..

#create pid and log dir if missing
mkdir -p $SSC_HOME/pids
mkdir -p $SSC_HOME/logs

PID_FILE=$SSC_HOME/pids/ssc.pid
LOG_DIR=$SSC_HOME/logs/

JAVA_DIR=$JAVA_HOME

if [[ -d "$JAVA_DIR" ]]; then
  echo "found java: $JAVA_DIR";
else
  if [[ -e "$(which java)" ]]; then
    JAVA_DIR=$(dirname $(which java))
  else
    echo "cannot locate a java anywhere, exiting"
    exit 1
  fi
fi

function start () {
  PROC_NAME=ssc-server-$HOSTNAME
  echo "starting: $PROC_NAME  $SSC_CLASSPATH"
  ALL_OPTS="-Dssc.name=$PROC_NAME $SSC_JVM_OPTIONS -Dssc.log.dir=$LOG_DIR  -cp $SSC_CLASSPATH "
  #"$JAVA_DIR"/bin/java $ALL_OPTS com.eqt.ssc.SimpleStateCollector
  nohup "$JAVA_DIR"/bin/java $ALL_OPTS com.eqt.ssc.SimpleStateCollector 2>&1 < /dev/null &
  echo $! > $PID_FILE
  echo SSC starting as process `cat $PID_FILE`.
}

if [[ "$1" == "start" ]]; then
 echo "starting";
 #check for existence first
 if [ -f $PID_FILE ]; then
  if kill -0 `cat $PID_FILE` > /dev/null 2>&1; then
    echo SSC is already running as process `cat $PID_FILE`.  Stop it first.
  else
    start
  fi
 else
  #TODO: a pgrep or something for the process name for lost processes?
  start
 fi

elif [[ "$1" == "stop" ]]; then
 echo "stopping";
 #check for existence first
 if [ -f $PID_FILE ]; then
  if kill -0 `cat $PID_FILE` > /dev/null 2>&1; then
   echo "stopped process";
  else
   echo "could not stop the process ${cat $PID_FILE}";
  fi
  rm $PID_FILE;
 else
  echo "no pid file, not running or lost process id.";
 fi
else
 echo "USAGE: ssc.sh {start|stop}"
fi

