#!/bin/bash
# ------------------------------------------------------------------
# Author: Palaniappan Kathiresan
# Title : Regulus Service Customer Post Install Script
#
# Description:
#
#         Regulus Service Customer Post Install Script.
#
# ------------------------------------------------------------------

# Application user & group
APP_USER="aeusapp"
APP_GROUP="aeusapp"
SERVICE_NAME="regulus-customer"
APP_PATH="/opt/davinta/regulus-service-customer"

#if [[ "$(readlink /proc/1/exe)" == */systemd ]]; then
#  cp -f ${APP_PATH}/bin/${SERVICE_NAME}.service /usr/lib/systemd/system/${SERVICE_NAME}.service
#  systemctl enable ${SERVICE_NAME}.service
#  systemctl daemon-reload
#  systemctl start ${SERVICE_NAME}.service
#else
  # Create soft link script to services directory
  ln -sf ${APP_PATH}/bin/${SERVICE_NAME} /etc/init.d/${SERVICE_NAME}

  if [ -x /sbin/chkconfig ]; then
    /sbin/chkconfig --add ${SERVICE_NAME}
  elif [ -x /usr/lib/lsb/install_initd ]; then
    /usr/lib/lsb/install_initd /etc/init.d/${SERVICE_NAME}
  else
     for i in 2 3 4 5; do
          ln -sf /etc/init.d/${SERVICE_NAME} /etc/rc.d/rc${i}.d/S90${SERVICE_NAME}
     done
     for i in 1 6; do
          ln -sf /etc/init.d/${SERVICE_NAME} /etc/rc.d/rc${i}.d/K10${SERVICE_NAME}
     done
  fi
  service ${SERVICE_NAME} start
#fi

exit 0
