#!/bin/bash
# ------------------------------------------------------------------
# Author: Palaniappan Kathiresan
# Title : Regulus Service Customer Pre Uninstall Script
#
# Description:
#
#         Regulus Service Customer Pre Uninstall Script.
#
# ------------------------------------------------------------------

# Application user & group
APP_USER="aeusapp"
APP_GROUP="aeusapp"
SERVICE_NAME="regulus-customer"
APP_PATH="/opt/davinta/regulus-service-customer"

# Uninstall => stop service
if [ "$1" = "0" ]; then
  if [ -s "/usr/lib/systemd/system/${SERVICE_NAME}.service" ]
  then
    systemctl stop ${SERVICE_NAME}.service
    systemctl disable ${SERVICE_NAME}.service
    systemctl daemon-reload
    rm /usr/lib/systemd/system/${SERVICE_NAME}.service
  elif [ -s "/etc/init.d/${SERVICE_NAME}" ]
  then
    service ${SERVICE_NAME} stop

    if [ -x /sbin/chkconfig ]; then
      /sbin/chkconfig --del ${SERVICE_NAME}
    elif [ -x /usr/lib/lsb/remove_initd ]; then
      /usr/lib/lsb/install_initd /etc/init.d/${SERVICE_NAME}
    else
      rm -f /etc/rc.d/rc?.d/???${SERVICE_NAME}
    fi

    rm /etc/init.d/${SERVICE_NAME}
  elif [ -f "${APP_PATH}/bin/${SERVICE_NAME}" ]
  then
    ${APP_PATH}/bin/${SERVICE_NAME} stop
  fi
fi

# Uninstall => remove soft links
if [ "$1" = "0" ]; then
  su - ${APP_USER} -c "rm ${APP_PATH}"  
fi

exit 0
