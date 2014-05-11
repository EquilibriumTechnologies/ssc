bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

#conf stuff to pull out to its own file?
HOSTNAME=`hostname`
SSC_JVM_OPTIONS=""

echo "libs at: $bin/../lib"

for x in $bin/../lib/*.jar; do
  SSC_CLASSPATH=$x:$SSC_CLASSPATH
done

export SSC_HOME="$bin"/..
PID_FILE=$SSC_HOME/pids/ssc.pid

function start () {
  PROC_NAME=ssc-server-$HOSTNAME
  echo "starting: $PROC_NAME  $SSC_CLASSPATH"
  "$JAVA_HOME"/bin/java -Dssc.name=$PROC_NAME $SSC_JVM_OPTIONS -cp $SSC_CLASSPATH com.eqt.ssc.SimpleStateCollector
  #nohup "$JAVA_HOME"/bin/java -Dssc.name=$PROC_NAME $SSC_JVM_OPTIONS -cp $SSC_CLASSPATH com.eqt.ssc.SimpleStateCapture  2>&1 < /dev/null &
  echo $! > $PID_FILE
  echo SSC starting as process `cat $PID_FILE`.
}

#check for existence first
if [ -f $PID_FILE ]; then
  if kill -0 `cat $PID_FILE` > /dev/null 2>&1; then
    echo SSC is already running as process `cat $PID_FILE`.  Stop it first.
  else
    start
  fi
else
  start
fi

