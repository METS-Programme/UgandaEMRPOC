var labqueue = labqueue || {};

labqueue.completeQueueDialog = null;
labqueue.addOrderWorkListDialog = null;
labqueue.readMessageDialog = null;
labqueue.alert_message_id=null;
labqueue.message=null;
labqueue.patientId=null;
labqueue.createMessageDialog=null;


labqueue.showCompleteQueueDialog = function (patientId) {
    labqueue.patientId = patientId;
    if (labqueue.completeQueueDialog == null) {
        labqueue.createCompletePatientQueueDialog();
    }
    labqueue.completeQueueDialog.show();
};

labqueue.closeDialog = function () {
    labqueue.completeQueueDialog.close();
};


labqueue.createCompletePatientQueueDialog = function () {
    labqueue.completeQueueDialog = emr.setupConfirmationDialog({
        selector: '#complete_patient_queue',
        actions: {
            confirm: function () {
                emr.getFragmentActionWithCallback('patientqueueing', 'completePatientQueue', 'completeQueue', visit.buildVisitTypeAttributeParams(),
                    function (v) {
                        jq('#complete_patient_queue' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                        labqueue.goToReturnUrl(labqueue.patientId, v);
                    });
            },
            cancel: function () {
                labqueue.completeQueueDialog.close();
            }
        }
    });

    labqueue.completeQueueDialog.close();
}


labqueue.showAddOrderToLabWorkLIstDialog = function (patientId) {
    labqueue.patientId = patientId;
    if (labqueue.addOrderWorkListDialog == null) {
        labqueue.createAddOrderToLabWorkListDialog();
    }
    jq("#patient_id").val(labqueue.patientId);
    labqueue.addOrderWorkListDialog.show();
};

labqueue.closeAddOrderToWorkDialog = function () {
    labqueue.addOrderWorkListDialog.close();
};


labqueue.createAddOrderToLabWorkListDialog = function () {
    labqueue.addOrderWorkListDialog = emr.setupConfirmationDialog({
        selector: '#add_patient_to_lab-worklist_dialog',
        actions: {
            cancel: function () {
                labqueue.addOrderWorkListDialog.close();
            }
        }
    });

    labqueue.addOrderWorkListDialog.close();
}


labqueue.showReadMessageDialog = function (message, alert_message_id) {
    labqueue.message = message;
    labqueue.alert_message_id=alert_message_id;
    jq("#message").html("");
    jq("#message").html(message);
    if (labqueue.readMessageDialog == null) {
        labqueue.createReadMessageDialog();
    }
    labqueue.readMessageDialog.show();
};

labqueue.closeReadMessageDialog = function () {
    labqueue.readMessageDialog.close();
};


labqueue.buildAlertAttributeParams = function () {
    var params = {};
    params['alert_message_id'] = labqueue.alert_message_id;
    return params;
}


labqueue.createReadMessageDialog = function () {
    labqueue.readMessageDialog = emr.setupConfirmationDialog({
        selector: '#read_message',
        actions: {
            confirm: function () {
                emr.getFragmentActionWithCallback('patientqueueing', 'alerts', 'markAlertAsRead', labqueue.buildAlertAttributeParams(),
                    function (v) {
                        jq('#read_message' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                        emr.navigateTo({ applicationUrl: emr.applyContextModel("patientqueueing/clinicianDashboard.page")});
                    });
            },
            cancel: function () {
                labqueue.readMessageDialog.close();
            }
        }
    });

    labqueue.readMessageDialog.close();
}


labqueue.showCreateMessageDialog = function (message, alert_message_id) {
    labqueue.message = message;
    labqueue.alert_message_id=alert_message_id;
    jq("#message").html("");
    jq("#message").html(message);
    if (labqueue.createMessageDialog == null) {
        labqueue.createReadMessageDialog();
    }
    labqueue.createMessageDialog.show();
};

labqueue.closeReadMessageDialog = function () {
    labqueue.createMessageDialog.close();
};


labqueue.buildAlertAttributeParams = function () {
    var params = {};
    params['alert_message_id'] = labqueue.alert_message_id;
    return params;
}