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
        var header = "<table><thead><tr><th>ID</th><th>NAMES</th><th>GENDER</th><th>DOB</th><th>VISIT STATUS</th><th>VISIT NO</th><th>ENTRY POINT</th><th>WAITING TIME</th></tr></thead><tbody>";
        var footer = "</tbody></table>";
        jq.each(response.patientTriageQueueList, function (index, element) {

                var patientQueueListElement = element;

                var dataRowTable = "";

                var waitingTime = getWaitingTime(patientQueueListElement.dateCreated);
                dataRowTable += "<tr>";
                dataRowTable += "<td><a href=''> " + patientQueueListElement.patientQueueId + "</a></td>";
                dataRowTable += "<td>" + patientQueueListElement.patientNames + "</td>";
                dataRowTable += "<td>" + patientQueueListElement.age + "</td>";
                dataRowTable += "<td></td>";
                dataRowTable += "<td></td>";
                dataRowTable += "<td></td>";
                dataRowTable += "<td>" + patientQueueListElement.locationFrom + "</td>";
                dataRowTable += "<td>" + waitingTime + "</td>";
                dataRowTable += "</tr>";
                if (element.status === "pending") {
                    stillInQueue += 1;
                    stillInQueueDataRows += dataRowTable;

                } else {
                    completedQueue += 1;
                    completedDataRows += dataRowTable;
                }
            }
        );

        if (stillInQueueDataRows !== "") {
            jq("#triage-queue-list-table").html("");
            jq("#triage-queue-list-table").append(header + stillInQueueDataRows + footer);

        }
        if (completedDataRows !== "") {
            jq("#triage-completed-list-tab").html("");
            jq("#triage-completed-list-tab").append(header + completedDataRows + footer);

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


<div class="info-header">
    <i class="icon-list-alt"></i>

    <h3 style="width: 50%">${ui.message("ugandaemrpoc.app.triage.patientqueue.title")}</h3> <span
        style="right:auto;width: 40%;font-weight: bold"></span>
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
        <div class="card">
            <div class="card-body">
                <form method="get" id="patient-triage-search-form" onsubmit="return false">
                    <input type="text" id="patient-triage-search" name="patient-triage-search"
                           placeholder="${ui.message ( "coreapps.findPatient.search.placeholder" )}"
                           autocomplete="off"/>
                </form>
            </div>
        </div>

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







