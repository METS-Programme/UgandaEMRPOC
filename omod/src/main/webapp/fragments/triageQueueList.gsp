<% if (currentLocation?.uuid?.equals(triageLocation)) { %>
<%
        ui.includeCss("coreapps", "patientsearch/patientSearchWidget.css")
        ui.includeJavascript("patientqueueing", "patientqueue.js")
        ui.includeJavascript("aijar", "js/aijar.js")
%>
<style>
.card-body {
    -ms-flex: 1 1 auto;
    flex: 7 1 auto;
    padding: 1.0rem;
    background-color: #eee;
}
</style>
<script>
    var stillInQueue = 0;
    var completedQueue = 0;
    jq(document).ready(function () {
        jq("#tabs").tabs();
    })
    if (jQuery) {
        jq(document).ready(function () {
            jq("#clinician-list").hide();
            getPatientQueue();
            jq("#patient-triage-search").change(function () {
                if (jq("#patient-triage-search").val().length >= 3) {
                    getPatientQueue();
                }
            });
        });
    }
    jq("form").submit(function (event) {
        alert("Handler for .submit() called.");
    });

    //GENERATION OF LISTS IN INTERFACE SUCH AS WORKLIST
    // Get Patients In Triage Queue
    function getPatientQueue() {
        jq("#triage-queue-list-table").html("");
        jq.get('${ ui.actionLink("getPatientQueueList") }', {
            triageSearchFilter: jq("#patient-triage-search").val().trim().toLowerCase()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("patientTriageQueueList=", "\"patientTriageQueueList\":").trim());
                displayTriageData(responseData);
            } else if (!response) {
                jq("#triage-queue-list-table").append(${ ui.message("coreapps.none ") });
            }
        });
    }

    function displayTriageData(response) {
        jq("#triage-queue-list-table").html("");
        var stillInQueueDataRows = "";
        var completedDataRows = "";
        stillInQueue = 0;
        completedQueue = 0;
        var headerPending = "<table><thead><tr><th>VISIT ID</th><th>NAMES</th><th>GENDER</th><th>AGE</th><th>VISIT STATUS</th><th>ENTRY POINT</th><th>WAITING TIME</th><th>ACTION</th></tr></thead><tbody>";
        var headerCompleted = "<table><thead><tr><th>VISIT ID</th><th>NAMES</th><th>GENDER</th><th>AGE</th><th>ENTRY POINT</th><th>COMPLETED TIME</th><th>ACTION</th></tr></thead><tbody>";
        var footer = "</tbody></table>";
        jq.each(response.patientTriageQueueList, function (index, element) {
                var patientQueueListElement = element;
                var dataRowTable = "";
                var vitalsPageLocation = "";
                if (element.status !== "completed") {
                    vitalsPageLocation = "/" + OPENMRS_CONTEXT_PATH + "/htmlformentryui/htmlform/enterHtmlFormWithStandardUi.page?patientId=" + patientQueueListElement.patientId + "&formUuid=d514be1d-8a95-4f46-b8d8-9b8485679f47&returnUrl=/openmrs/patientqueueing/clinicianDashboard.page";
                }else {
                    vitalsPageLocation = "/" + OPENMRS_CONTEXT_PATH + "/htmlformentryui/htmlform/editHtmlFormWithStandardUi.page?patientId=" + patientQueueListElement.patientId + "&formUuid=d514be1d-8a95-4f46-b8d8-9b8485679f47&encounterId="+patientQueueListElement.encounterId+"&returnUrl=/openmrs/patientqueueing/clinicianDashboard.page";
                }

                var action = "<i style=\"font-size: 25px;\" class=\"icon-edit edit-action\" title=\"Goto Patient Dashboard\" onclick=\" location.href = '" + vitalsPageLocation + "'\"></i>";

                var waitingTime = getWaitingTime(patientQueueListElement.dateCreated);
                dataRowTable += "<tr>";
                dataRowTable += "<td>" + patientQueueListElement.queueNumber.substring(15) + "</td>";
                dataRowTable += "<td>" + patientQueueListElement.patientNames + "</td>";
                dataRowTable += "<td>" + patientQueueListElement.gender + "</td>";
                dataRowTable += "<td>" + patientQueueListElement.age + "</td>";
                if (element.status !== "completed") {

                    if (patientQueueListElement.priorityComment != null) {
                        dataRowTable += "<td>" + patientQueueListElement.priorityComment + "</td>";
                    } else {
                        dataRowTable += "<td></td>";
                    }
                }
                dataRowTable += "<td>" + patientQueueListElement.locationFrom.substring(0, 3) + "</td>";
                dataRowTable += "<td>" + waitingTime + "</td>";
                dataRowTable += "<td>" + action + "</td>";
                dataRowTable += "</tr>";
                if (element.status === "pending") {
                    stillInQueue += 1;
                    stillInQueueDataRows += dataRowTable;

                } else if (element.status === "completed") {
                    completedQueue += 1;
                    completedDataRows += dataRowTable;
                }
            }
        )
        ;

        if (stillInQueueDataRows !== "") {
            jq("#triage-queue-list-table").html("");
            jq("#triage-queue-list-table").append(headerPending + stillInQueueDataRows + footer);

        }
        if (completedDataRows !== "") {
            jq("#triage-completed-list-tab").html("");
            jq("#triage-completed-list-tab").append(headerCompleted + completedDataRows + footer);

        }
        jq("#triage-pending-number").html("");
        jq("#triage-pending-number").append(stillInQueue);
        jq("#triage-completed-number").html("");
        jq("#triage-completed-number").append(completedQueue);
    }

    //SUPPORTIVE FUNCTIONS//
    //Get Waiting Time For Patient In Queue
    function getWaitingTime(queueDate) {
        var diff = Math.abs(new Date() - new Date(queueDate));
        var seconds = Math.floor(diff / 1000); //ignore any left over units smaller than a second
        var minutes = Math.floor(seconds / 60);
        seconds = seconds % 60;
        var hours = Math.floor(minutes / 60);
        minutes = minutes % 60;
        return hours + ":" + minutes + ":" + seconds
    }
</script>
<br/>

<div class="card">
    <div class="card-body">
        <div><h1><i class="icon-list-alt">${ui.message("ugandaemrpoc.app.triage.patientqueue.title")}</i></h1></div>

        <form method="get" id="patient-triage-search-form" onsubmit="return false">
            <input type="text" id="patient-triage-search" name="patient-triage-search"
                   placeholder="${ui.message("coreapps.findPatient.search.placeholder")}"
                   autocomplete="off"/>
        </form>
    </div>
</div>

<div id="tabs">
    <ul>
        <li>
            <a href="#queue-triage-tab">
                WAITING QUEUE <span style="color:red" id="triage-pending-number">5</span>
            </a>
        </li>
        <li>
            <a href="#triage-completed-list-tab">
                COMPLETED <span style="color:red" id="triage-completed-number">5</span>
            </a>
        </li>
    </ul>
    <section sectionTag="section" id="queue-triage-tab" headerTag="h1">
        <div class="info-body">
            <div id="triage-queue-list-table">
            </div>
        </div>
    </section>
    <section sectionTag="section" id="triage-completed-list-tab" headerTag="h1">
        List of Completed Patients
    </section>
</div>
<% } %>







