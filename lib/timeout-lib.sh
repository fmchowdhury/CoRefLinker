#!/bin/bash

# usage:
# . $0
# set_timeout 1m
# [... do whatever ...]
# cleanup_timeout

# http://www.andyd.net/2006/handling-timeout-properly/

# spawn chronometer child
function set_timeout() {
    TIMEOUT=${1:-"10s"}
    sleep $TIMEOUT && kill -s ALRM $$ &
    CHRONO_CHILD_PID=$!
    trap timeup SIGALRM
}

function timeup() {
    echo "Maximum allowed time ($TIMEOUT) elapsed... aborting $$!"
    exit 1
}

function cleanup_timeout() {
    # cleanup child if still exists
    [ -d /proc/$CHRONO_CHILD_PID ] && kill $CHRONO_CHILD_PID
}
