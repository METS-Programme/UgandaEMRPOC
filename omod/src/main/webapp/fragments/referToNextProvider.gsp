<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>

<script>
    jq(document).ready(function () {
        jq("#provider_id_container").addClass('hidden');
        jq("#patient_status_container").addClass('hidden');
        jq("#visit_comment_container").addClass('hidden');
        jq("#location_id").change(function () {
            if (jq("#location_id").val() !== "ff01eaab-561e-40c6-bf24-539206b521ce") {
                jq("#provider_id_container").removeClass('hidden');
                jq("#patient_status_container").removeClass('hidden');
                jq("#visit_comment_container").removeClass('hidden');
            } else {
                jq("#provider_id_container").addClass('hidden');
                jq("#patient_status_container").addClass('hidden');
                jq("#visit_comment_container").addClass('hidden');
            }
        });

        beforeSubmit.push(function () {
            var locationTo = jq("#location_id").val();
            var providerTo = jq("#provider_id").val();
            var patientStatus = jq("#patient_status").val();
            var visitComment = jq("#visit_comment").val();
            sendPatientToNextLocation(locationTo, providerTo, patientStatus, visitComment, patientId);
            return true;
        });
    });

    function sendPatientToNextLocation(locationTo, providerTo, patientStatus, visitComment, patientId) {
        jq.post('${ ui.actionLink("ugandaemrpoc","referToNextProvider","post") }', {
            patientId: patientId,
            locationId: locationTo,
            providerId: providerTo,
            patientStatus: patientStatus,
            visitComment: visitComment
        }, function (response) {
            if (response) {

            } else if (!response) {
            }
        });
    }

    function printTriageRecord(divIdToPrint, dataToPrint) {
        var divToPrint = document.getElementById(divIdToPrint);
        var newWin = window.open('', 'Print-Window');
        var checkInData = "";
        jq("#check_in_receipt").html("");
        if (dataToPrint.patientTriageQueue !== "") {
            checkInData += "<table>";
            checkInData += "<tr><th width=\"50%\" style=\"text-align: left\">Check In Date:</th><td>%s</td></tr>".replace("%s", dataToPrint.patientTriageQueue.dateCreated);
            checkInData += "<tr><th width=\"50%\" style=\"text-align: left\">Patient Names:</th><td>%s</td></tr>".replace("%s", dataToPrint.patientTriageQueue.patientNames);
            checkInData += "<tr><th width=\"50%\" style=\"text-align: left\">Visit No.:</th><td>%s</td></tr>".replace("%s", dataToPrint.patientTriageQueue.queueNumber.substring(15));
            checkInData += "<tr><th width=\"50%\" style=\"text-align: left\">Gender:</th><td>%s</td></tr>".replace("%s", dataToPrint.patientTriageQueue.gender);
            checkInData += "<tr><th width=\"50%\" style=\"text-align: left\">Entry Point:</th><td>%s</td></tr>".replace("%s", dataToPrint.patientTriageQueue.locationFrom.substring(0, 3));
            checkInData += "<tr><th width=\"50%\" style=\"text-align: left\">Registration Attendant:</th><td>%s</td></tr>".replace("%s", dataToPrint.patientTriageQueue.creatorNames);
            checkInData += "</table>";
        }
        jq("#check_in_receipt").append(checkInData);
        newWin.document.open();
        newWin.document.write('<html><body onload="window.print()">' + divToPrint.innerHTML + '</body></html>');
        newWin.document.close();
        setTimeout(function () {
            newWin.close();
        }, 10);
    }
</script>

<script>
    if (jQuery) {
    }
</script>

<div class="container">
    <div class="row">
        <div class="col-5">Next Service Point:</div>

        <div class="col-7">
            <div class="form-group">
                <select class="form-control" id="location_id" name="location_id">
                    <option value="">Select Next Service Point</option>
                    <% if (locationList != null) {
                        locationList.each { %>
                    <option value="${it.uuid}">${it.name}</option>
                    <%
                            }
                        }
                    %>
                </select>
                <span class="field-error" style="display: none;"></span>
                <% if (locationList == null) { %>
                <div><${ui.message("patientqueueing.select.error")}</div>
                <% } %>
            </div>
        </div>
    </div>

    <div class="row" id="provider_id_container">
        <div class="col-5">Provider:</div>

        <div class="col-7">
            <div class="form-group">
                <select class="form-control" id="provider_id" name="provider_id">
                    <option value="">Select Provider</option>
                    <% if (providerList != null) {
                        providerList.each { %>
                    <option value="${it.providerId}">${it.name}</option>
                    <%
                            }
                        }
                    %>
                </select>
                <span class="field-error" style="display: none;"></span>
                <% if (providerList == null) { %>
                <div><${ui.message("patientqueueing.select.error")}</div>
                <% } %>
            </div>
        </div>
    </div>

    <div class="row" id="patient_status_container">
        <div class="col-4">Patient Status:</div>

        <div class="col-8">
            <div class="form-group">
                <select class="form-control" id="patient_status" name="patient_status">
                    <option value="">Select Patient Status</option>
                    <option value="normal">Normal</option>
                    <option value="emergency">Emergency</option>
                </select>
                <span class="field-error" style="display: none;"></span>
            </div>
        </div>
    </div>

    <div class="row" id="visit_comment_container">
        <div class="col-4">Visit Type:</div>

        <div class="col-8">
            <div class="form-group">
                <select class="form-control" id="visit_comment" name="visit_comment">
                    <option value="">Select Visit Type</option>
                    <option value="new visit">New Visit</option>
                    <option value="revisit">Revisit</option>
                </select>
                <span class="field-error" style="display: none;"></span>
            </div>
        </div>
    </div>
</div>



