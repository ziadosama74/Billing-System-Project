#!/bin/bash

# ================================
# CONFIG
# ================================
export JAVA_HOME=$HOME/portable-java/current
export PATH=$JAVA_HOME/bin:$PATH
MAVEN=$HOME/portable-netbeans/current/java/maven/bin/mvn
SESSION="billing_system"

# ================================
# CLEAN OLD SESSION
# ================================
tmux kill-session -t $SESSION 2>/dev/null

# kill port 5000 if used
PID=$(lsof -ti:5000)
if [ ! -z "$PID" ]; then
  kill -9 $PID
fi

# ================================
# CREATE TMUX SESSION
# ================================
tmux new-session -d -s $SESSION

# split into 3 panes
tmux split-window -h        # right
tmux split-window -v        # bottom right
clear;
# ================================
# SERVER (pane 0)
# ================================
tmux send-keys -t $SESSION:0.0 "
clear;
cd $HOME/Billing-System-Project/CDR_Parsing_Files_Process;

# silent compile
$MAVEN clean compile > /dev/null 2>&1;

# clear screen after compile
clear;
# run app (quiet mode)
$MAVEN -q exec:java -Dexec.mainClass=Main_Process.CDR_Parsing_Files_Process;
" C-m

# ================================
# CLIENT (pane 1)
# ================================
tmux send-keys -t $SESSION:0.1 "
sleep 1;
clear;
cd $HOME/Billing-System-Project/CDR_Rating_Process;

# silent compile
$MAVEN clean compile > /dev/null 2>&1;

# clear screen after compile
clear;
# run app (quiet mode)
$MAVEN -q exec:java -Dexec.mainClass=Main_Process.CDR_Rating_Process;
" C-m

# ================================
# CONTROL PANEL (pane 2)
# ================================
tmux send-keys -t $SESSION:0.2 "
clear;
bash;
clear;
" C-m

# ================================
# ATTACH SESSION
# ================================
tmux attach -t $SESSION
