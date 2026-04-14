#!/bin/bash

# ================================
# CONFIG
# ================================
export JAVA_HOME=$HOME/portable-java/current
export PATH=$JAVA_HOME/bin:$PATH
MAVEN=$HOME/portable-netbeans/current/java/maven/bin/mvn
SESSION="billing_system"

# kill old session
tmux kill-session -t $SESSION 2>/dev/null

# kill port
PID=$(lsof -ti:5000)
if [ ! -z "$PID" ]; then
  kill -9 $PID
fi

# ================================
# CREATE SESSION
# ================================
tmux new-session -d -s $SESSION

# split to 3 panes
tmux split-window -h        # right
tmux split-window -v        # bottom right

# ================================
# SERVER (pane 0)
# ================================
tmux send-keys -t $SESSION:0.0 "
echo 'SERVER RUNNING';
cd $HOME/Billing-System-Project/CDR_Parsing_Files_Process;
$MAVEN clean compile exec:java -Dexec.mainClass=Main_Process.CDR_Parsing_Files_Process
" C-m

# ================================
# CLIENT (pane 1)
# ================================
tmux send-keys -t $SESSION:0.1 "
sleep 3;
echo 'CLIENT RUNNING';
cd $HOME/Billing-System-Project/CDR_Rating_Process;
$MAVEN clean compile exec:java -Dexec.mainClass=Main_Process.CDR_Rating_Process
" C-m

# ================================
# CONTROL PANEL (pane 2)
# ================================
tmux send-keys -t $SESSION:0.2 "
echo '=== CONTROL PANEL ===';
echo 'Stop system: tmux kill-session -t $SESSION';
echo 'Or: pkill -f CDR_';
echo 'Detach: Ctrl+B then D';
bash
" C-m

# attach
tmux attach -t $SESSION
