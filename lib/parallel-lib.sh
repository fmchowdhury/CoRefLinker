#!/bin/sh

#================================================================
# Manage parallel execution of N processes
#================================================================
# Usage:
# . $0
# <do whatever loop>
#    <some command in background> &
#    enqueue_and_wait_for_free_slot <N> <CHECK_INTERVAL>
# <end loop>
#
# <N> = maximum number of processes concurrently spawned in parallel
# 	(default = 4)
# <CHECK_INTERVAL> = sleep period length between two checks for
#	end of processes (default = 10s)
#	Must be a legal argument to 'sleep' command

NUM=0
QUEUE=""
DEFAULT_SLEEP_TICK=10s
DEFAULT_MAX_PROCESSES=4

function echoqueue {
    for PID in $QUEUE
    do
	echo -n "$PID " 1>&2
    done
    echo 1>&2
}

function queue {
    QUEUE="$QUEUE
$1"
    NUM=$(($NUM+1))
    echo -n "QUEUE " 1>&2; echoqueue
}

function dequeue {
    OLDDEQUEUE=$QUEUE
    QUEUE=""
    for PID in $OLDDEQUEUE
    do
	if [ ! "$PID" = "$1" ] ; then
	    QUEUE="$QUEUE
$PID"
	fi
    done
    NUM=$(($NUM-1))
    echo -n "DEQUEUE " 1>&2; echoqueue
}

function checkqueue {
    OLDCHQUEUE=$QUEUE
    for PID in $OLDCHQUEUE
    do
	if [ ! -d /proc/$PID ] ; then
	    dequeue $PID
	fi
    done
    echo -n "CHECKQUEUE " 1>&2; echoqueue
}

function enqueue_and_wait_for_free_slot {
    MAX_PROCESSES=${1:-"$DEFAULT_MAX_PROCESSES"}
    SLEEP_TICK=${2:-"$DEFAULT_SLEEP_TICK"}
    PID=$!
    queue $PID

    while [ $NUM -ge $MAX_PROCESSES ] # MAX PROCESSES
    do
	checkqueue
	sleep $SLEEP_TICK
    done
}

function wait_until_queue_is_empty {
    SLEEP_TICK=${1:-"$DEFAULT_SLEEP_TICK"}
    while [ $NUM -gt 0 ] # queue empty?
    do
	checkqueue
	sleep $SLEEP_TICK
    done
}

#================================================================
