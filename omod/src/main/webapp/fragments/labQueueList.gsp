<%
    ui.includeCss("coreapps", "patientsearch/patientSearchWidget.css")
    ui.includeJavascript("patientqueueing", "patientqueue.js")
    ui.includeJavascript("aijar", "js/aijar.js")
%>
<style>
.div-table {
    display: table;
    width: 100%;
}

.div-row {
    display: table-row;
    width: 100%;
}

.div-col1 {
    display: table-cell;
    margin-left: auto;
    margin-right: auto;
    width: 100%;
}

.div-col2 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 50%;
}

.div-col3 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 33%;
}

.div-col4 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 25%;
}

.div-col5 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 20%;
}

.div-col6 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 16%;
}

.dialog {
    width: 550px;
}
</style>
<script>
    jq(document).ready(function () {
        jq("#tabs").tabs();
    })
    if (jQuery) {
        jq(document).ready(function () {
            jq("#clinician-list").hide();
            getPatientLabQueue();
            getOrders();
            getResults();
            setSpecimenSource();
            jq("#patient-lab-search").change(function () {
                if (jq("#patient-lab-search").val().length >= 3) {
                    getPatientLabQueue();
                }
            });

            jq("#reference-lab-container").addClass('hidden');

            jq("#refer_test").change(function () {
                if (jq("#refer_test").is(":checked")) {
                    jq("#reference-lab-container").removeClass('hidden');
                } else {
                    jq("#reference-lab-container").addClass('hidden');
                }
            });

            jq("#submit-schedule").click(function () {
                jq.get('${ ui.actionLink("scheduleTest") }', {
                    orderId: jq("#order_id").val().trim().toLowerCase(),
                    sampleId: jq("#sample_id").val().trim().toLowerCase(),
                    referTest: jq("#refer_test").val().trim().toLowerCase(),
                    referenceLab: jq("#reference_lab").val().trim().toLowerCase(),
                    specimenSourceId: jq("#specimen_source_id").val().trim().toLowerCase()
                }, function (response) {
                    if (!response) {
                        ${ ui.message("coreapps.none ") }
                    }
                });
            });


        });
    }

    jq("form").submit(function (event) {
        alert("Handler for .submit() called.");
    });

    //GENERATION OF LISTS IN INTERFACE SUCH AS WORKLIST
    // Get Patients In Lab Queue
    function getPatientLabQueue() {
        jq("#lab-queue-list-table").html("");
        jq.get('${ ui.actionLink("getPatientQueueList") }', {
            labSearchFilter: jq("#patient-lab-search").val().trim().toLowerCase()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("patientLabQueueList=", "\"patientLabQueueList\":").trim());
                displayLabData(responseData);
            } else if (!response) {
                jq("#lab-queue-list-table").append(${ ui.message("coreapps.none ") });
            }
        });
    }

    // Gets Orders of List of WorkList and Refered Tests
    function getOrders() {
        jq.get('${ ui.actionLink("getOrders") }', {
            date: (new Date()).toString()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("ordersList=", "\"ordersList\":").trim());
                displayLabOrder(responseData)
            }
        });
    }

    // Gets Orders with results for The List of results
    function getResults() {
        jq.get('${ ui.actionLink("getOrderWithResult") }', {
            date: (new Date()).toString()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("ordersList=", "\"ordersList\":").trim());
                displayLabResult(responseData)
            }
        });
    }

    function displayLabData(response) {
        var content = "";
        content = "<table><thead><tr><th>Q ID</th><th>Names</th><th>Age</th><th>ORDER FROM</th><th>WAITING TIME</th><th>TEST(S) ORDERED</th></tr></thead><tbody>";
        jq.each(response.patientLabQueueList, function (index, element) {
                var orders = displayLabOrderData(element, true);
                if (orders !== null) {
                    var patientQueueListElement = element;
                    var waitingTime = getWaitingTime(patientQueueListElement.dateCreated);
                    content += "<tr>";
                    content += "<td>" + patientQueueListElement.patientQueueId + "</td>";
                    content += "<td>" + patientQueueListElement.patientNames + "</td>";
                    content += "<td>" + patientQueueListElement.age + "</td>";
                    content += "<td>" + patientQueueListElement.providerNames + " - " + patientQueueListElement.locationFrom + "</td>";
                    content += "<td>" + waitingTime + "</td>";
                    content += "<td>";
                    content += orders;
                    content += "</td>";
                    content += "</tr>";
                }
            }
        );
        content += "</tbody></table>";
        jq("#lab-queue-list-table").append(content);
    }

    function displayLabOrderData(labQueueList, removeProccesedOrders) {
        var header = "<table><thead></thead><tbody>";
        var footer = "</tbody></table>";
        var orderedTestsRows = "";
        var urlToPatientDashBoard = '${ui.pageLink("coreapps","clinicianfacing/patient",[patientId: "patientIdElement"])}'.replace("patientIdElement", labQueueList.patientId);
        jq.each(labQueueList.orderMapper, function (index, element) {
            if (removeProccesedOrders !== false && element.accessionNumber === null && element.status === "active") {
                var urlTransferPatientToAnotherQueue = 'patientqueue.showAddOrderToLabWorkLIstDialog("patientIdElement")'.replace("patientIdElement", element.orderNumber);
                orderedTestsRows += "<tr>";
                orderedTestsRows += "<td>" + element.orderNumber + "</td>";
                orderedTestsRows += "<td>" + element.conceptName + "</td>";
                orderedTestsRows += "<td>" + element.urgency + "</td>";
                orderedTestsRows += "<td>";
                orderedTestsRows += "<i class=\"icon-dashboard view-action\" title=\"Goto Patient's Dashboard\" onclick=\"location.href = 'urlToPatientDashboard'\"></i>".replace("urlToPatientDashboard", urlToPatientDashBoard);
                orderedTestsRows += "<i class=\"icon-tags edit-action\" title=\"Transfer To Another Provider\" onclick='urlTransferPatientToAnotherQueue'></i>".replace("urlTransferPatientToAnotherQueue", urlTransferPatientToAnotherQueue);
                orderedTestsRows += "</td>";
                orderedTestsRows += "</tr>";
            }
        });
        if (orderedTestsRows !== "") {
            return header + orderedTestsRows + footer;
        } else {
            return null;
        }
    }

    function displayLabOrder(labQueueList) {
        var referedTests = "";
        var workListTests = "";

        var tableHeader = "<table><thead><tr><th>ORDER NO</th><th>PATIENT NAME</th><th>TEST</th><th>STATUS</th><th>URGENCY</th><th>ACTION</th></tr></thead><tbody>";

        var tableFooter = "</tbody></table>";

        jq.each(labQueueList.ordersList, function (index, element) {
            var orderedTestsRows = "";
            var instructions = element.instructions;
            var actionIron = "";
            var actionURL = "";
            if (instructions != null && instructions.toLowerCase().indexOf("refer to") >= 0) {
                actionIron = "icon-tags edit-action";
                actionURL = 'patientqueue.showAddOrderToLabWorkLIstDialog("patientIdElement")'.replace("patientIdElement", element.orderId);
            } else {
                actionIron = "icon-tags edit-action";
                actionURL = 'patientqueue.showAddOrderToLabWorkLIstDialog("patientIdElement")'.replace("patientIdElement", element.orderId);
            }
            orderedTestsRows += "<tr>";
            orderedTestsRows += "<td>" + element.orderNumber + "</td>";
            orderedTestsRows += "<td>" + element.patient + "</td>";
            orderedTestsRows += "<td>" + element.conceptName + "</td>";
            orderedTestsRows += "<td>" + element.status + "</td>";
            orderedTestsRows += "<td>" + element.urgency + "</td>";
            orderedTestsRows += "<td>";
            orderedTestsRows += "<a title=\"Edit Result\" onclick='showEditResultForm(" + element.orderId + ")'><i class=\"icon-list-ul small\"></i></a>";
            orderedTestsRows += "<i class=\" + actionIron + \" title=\"Transfer To Another Provider\" onclick='urlTransferPatientToAnotherQueue'></i>".replace("urlTransferPatientToAnotherQueue", actionURL);
            orderedTestsRows += "</td>";
            orderedTestsRows += "</tr>";
            if (element.status !== "has results") {
                if (instructions != null && instructions.toLowerCase().indexOf("refer to") >= 0) {
                    referedTests += orderedTestsRows;
                } else {
                    workListTests += orderedTestsRows;
                }
            }
        });

        jq("#lab-work-list-tab").html("");
        jq("#referred-tests-tab").html("");

        if (workListTests.length > 0) {
            jq("#lab-work-list-tab").append(tableHeader + workListTests + tableFooter);
        } else {
            jq("#lab-work-list-tab").append("No Data");
        }

        if (referedTests.length > 0) {
            jq("#referred-tests-tab").append(tableHeader + referedTests + tableFooter);
        } else {
            jq("#referred-tests-tab").append("No Data ");
        }
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

    //Sets the Specimen Source Options in the Select in the scheduleTestDialogue
    function setSpecimenSource() {
        jq("#error-specimen-source").html("");
        jq("#specimen_source_id").html("");
        var content = "";
        content += "<option value=\"\">" + "${ui.message("Specimen Source")}" + "</option>";
        <% if (specimenSource.size() > 0) {
                      specimenSource.each { %>
        content += "<option value=\"${it.conceptId}\">" + "${it.getName().name}" + "</option>";
        <%} }else {%>
        jq("#error-specimen-source").append(${ui.message("patientqueueing.select.error")});
        <%}%>
        jq("#specimen_source_id").append(content);
    }

    // Generates Sample ID for the Sample ID Field on the scheduleTestDialogue
    function generateSampleId() {
        jq.get('${ ui.actionLink("generateSampleID") }', {
            orderId: jq("#order_id").val().trim().toLowerCase()
        }, function (response) {
            if (response) {
                var responseData = response.replace("{defaultSampleId=\"", "").replace("\"}", "").trim();
                jq("#sample_id").val(responseData);
            }
        });
    }
</script>
${ui.includeFragment("ugandaemrpoc", "lab/diplayResultList")}
<div class="info-header">
    <i class="icon-beaker"></i>
    <h3 style="width: 50%">${ui.message("ugandaemrpoc.app.lab.patientqueue.title")}</h3> <span
        style="right:auto;width: 40%;font-weight: bold"></span>
</div>

<div id="tabs">
    <ul>
        <li>
            <a href="#queue-lab-tab">
                TESTS ORDERED
            </a>
        </li>
        <li>
            <a href="#lab-work-list-tab">
                WORK LIST
            </a>
        </li>
        <li>
            <a href="#referred-tests-tab">
                REFERRED TESTS
            </a>
        </li>
        <li>
            <a href="#lab-results-tab">
                RESULTS
            </a>
        </li>
    </ul>
    <section sectionTag="section" id="queue-lab-tab" headerTag="h1">
        <span>
            <form method="get" id="patient-lab-search-form" onsubmit="return false">
                <input type="text" id="patient-lab-search" name="patient-lab-search"
                       placeholder="${ui.message("coreapps.findPatient.search.placeholder")}" autocomplete="off"/>

            </form>
        </span>

        <div class="info-body">
            <div id="lab-queue-list-table">
            </div>
        </div>

    </section>
    <section sectionTag="section" id="lab-work-list-tab" headerTag="h1">
        List Of Acccepted Tests Go Here
    </section>
    <section sectionTag="section" id="referred-tests-tab" headerTag="h1">
        List of Tests Referred To Other Lab go here
    </section>
    <section sectionTag="section" id="lab-results-tab" headerTag="h1">
        Lab Results To Tests Go Here
    </section>
</div>

${ui.includeFragment("ugandaemrpoc", "lab/scheduleTestDialogue")}
${ui.includeFragment("ugandaemrpoc", "lab/resultForm")}
${ui.includeFragment("ugandaemrpoc", "printResults")}







