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

            getSpecimenSource();
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

    /**
     * Show the container, and enable all elements in it
     * @param containerId
     */
    function showContainer(containerId) {
        jq(containerId).removeClass('hidden');
        jq(containerId + ' :input').attr('disabled', false);
        jq(containerId + ' :input').prop('checked', false);
    }

    /**
     * Hide the container, and disables all elements in it
     * @param containerId
     */
    function hideContainer(containerId) {
        jq(containerId).addClass('hidden');
        jq(containerId + ' :input').attr('disabled', true);
        jq(containerId + ' :input').prop('checked', false);
    }

    function getWaitingTime(queueDate) {
        var diff = Math.abs(new Date() - new Date(queueDate));
        var seconds = Math.floor(diff / 1000); //ignore any left over units smaller than a second
        var minutes = Math.floor(seconds / 60);
        seconds = seconds % 60;
        var hours = Math.floor(minutes / 60);
        minutes = minutes % 60;
        return hours + ":" + minutes + ":" + seconds
    }

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

    function getSpecimenSource() {
        var content = "";
        content += "<option value=\"\">";
        content += "${ui.message("Specimen Source")}";
        content += +"</option>";

        <% if (specimenSource.size() > 0) {
                      specimenSource.each { %>
        content += "<option value=\"${it.conceptId}\">";
        content += "${it.getName().name}";
        content += +"</option>";
        <%} }%>
        return content
    }

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

    function getResults() {
        jq.get('${ ui.actionLink("getResults") }', {
            date: (new Date()).toString()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("resultsList=", "\"resultsList\":").trim());
                displayLabOrder(responseData)
            }
        });
    }

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

    function displayLabData(response) {
        var content = "";
        content = "<table><thead><tr><th>Q ID</th><th>Names</th><th>Age</th><th>ORDER FROM</th><th>WAITING TIME</th><th>TEST(S) ORDERED</th></tr></thead><tbody>";
        jq.each(response.patientLabQueueList, function (index, element) {
                var orders = displayLabOrderData(element, true);
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
        );
        content += "</tbody></table>";
        jq("#lab-queue-list-table").append(content);
    }

    function displayLabOrderData(labQueueList, removeProccesedOrders) {
        var orderedTests = "";
        orderedTests = "<table><thead></thead><tbody>";
        var urlToPatientDashBoard = '${ui.pageLink("coreapps","clinicianfacing/patient",[patientId: "patientIdElement"])}'.replace("patientIdElement", labQueueList.patientId);
        var noOfOrders = 0;
        jq.each(labQueueList.orderMapper, function (index, element) {
            if (removeProccesedOrders !== false && element.accessionNumber === null && element.status === "active") {
                var urlTransferPatientToAnotherQueue = 'patientqueue.showAddOrderToLabWorkLIstDialog("patientIdElement")'.replace("patientIdElement", element.orderNumber);
                orderedTests += "<tr>";
                orderedTests += "<td>" + element.orderNumber + "</td>";
                orderedTests += "<td>" + element.conceptName + "</td>";
                orderedTests += "<td>" + element.urgency + "</td>";
                orderedTests += "<td>";
                orderedTests += "<i class=\"icon-dashboard view-action\" title=\"Goto Patient's Dashboard\" onclick=\"location.href = 'urlToPatientDashboard'\"></i>".replace("urlToPatientDashboard", urlToPatientDashBoard);
                orderedTests += "<i class=\"icon-tags edit-action\" title=\"Transfer To Another Provider\" onclick='urlTransferPatientToAnotherQueue'></i>".replace("urlTransferPatientToAnotherQueue", urlTransferPatientToAnotherQueue);
                orderedTests += "</td>";
                orderedTests += "</tr>";
                noOfOrders = +noOfOrders
            }
        });
        orderedTests += "</tbody></table>";

        return orderedTests
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
            "click: showResultForm, attr: { href : '#' }"
            orderedTestsRows += "<td>";
            orderedTestsRows += "<a title=\"Edit Result\" onclick='showEditResultForm(" + element.orderId + ")'><i class=\"icon-list-ul small\"></i></a>";
            orderedTestsRows += "<i class=\" + actionIron + \" title=\"Transfer To Another Provider\" onclick='urlTransferPatientToAnotherQueue'></i>".replace("urlTransferPatientToAnotherQueue", actionURL);
            orderedTestsRows += "</td>";
            orderedTestsRows += "</tr>";
            if (instructions != null && instructions.toLowerCase().indexOf("refer to") >= 0) {
                referedTests += orderedTestsRows;
            } else {
                workListTests += orderedTestsRows;
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
</script>

<div class="info-header">
    <i class="icon-diagnosis"></i>

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
                REFEREED TESTS
            </a>
        </li>
        <li>
            <a href="#lab-results">
                RESULTS
            </a>
        </li>
    </ul>
    <section sectionTag="section" id="queue-lab-tab" headerTag="h1">
        <span>
            <form method="get" id="patient-lab-search-form" onsubmit="return false">
                <input type="text" id="patient-lab-search" name="patient-lab-search"
                       placeholder="${ui.message("coreapps.findPatient.search.placeholder")}" autocomplete="off"/><i
                    id="patient-search-clear-button" class="small icon-remove-sign"></i>
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
    <section sectionTag="section" id="lab-results" headerTag="h1">
        Lab Results To Tests Go Here
    </section>
</div>

<div id="add-order-to-lab-worklist-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-reorder"></i>

        <h3>${ui.message("SCHEDULE TEST")}</h3>
    </div>

    <div>
        <form id="addtesttoworklist">
            <fieldset style="min-width: 90%">
                <span id="add_to_queue-container">
                    <input type="hidden" id="order_id" name="order_id" value="">
                </span>

                <div class="div-table">
                    <div class="div-row">
                        <div class="div-col3">
                            <label for="sample_id">
                                <span>${ui.message("SPECIMEN ID/SAMPLE ID")}</span>
                            </label>
                            <input type="text" id="sample_id" name="sample_id" value="">
                            <a onclick="generateSampleId()"><i class=" icon-barcode">Generate Sample Id</i></a>
                        </div>

                        <div class="div-col3">
                            <span id="specimen-source-container">
                                <label for="specimen_source_id">
                                    <span>${ui.message("SAMPLE TYPE")}</span>
                                </label>
                                <select name="specimen_source_id" id="specimen_source_id">
                                    <option value="">${ui.message("Specimen Source")}</option>
                                    <% if (specimenSource.size() > 0) {
                                        specimenSource.each { %>
                                    <option value="${it.conceptId}">${it.getName().name}</option>
                                    <% }
                                    } %>
                                </select>
                                <span class="field-error" style="display: none;"></span>
                                <% if (specimenSource == null) { %>
                                <div><${ui.message("patientqueueing.select.error")}</div>
                                <% } %>
                            </span>
                        </div>
                    </div>
                    <br/><br/>

                    <div class="div-row">
                        <div class="div-col3">
                            <label for="refer_test">
                                <span>${ui.message("REFER TEST")}</span>
                            </label>
                            <input type="checkbox" name="refer_test" id="refer_test">
                        </div>

                        <div class="div-col3">
                            <span id="reference-lab-container">
                                <label for="reference_lab">
                                    <span>${ui.message("REFERENCE LAB")}</span>
                                </label>
                                <select name="reference_lab" id="reference_lab">
                                    <option value="">${ui.message("Select Reference Lab")}</option>
                                    <option value="cphl">CPHL</option>
                                    <option value="uvri">UVRI</option>
                                </select>
                                <span class="field-error" style="display: none;"></span>
                            </span>
                        </div>
                    </div>
                </div>

                <div class="dialog-content form">
                    <button class="cancel" id="">${ui.message("patientqueueing.close.label")}</button>
                    <button type="submit" class="confirm"
                            id="submit-schedule">${ui.message("patientqueueing.send.label")}</button>
                </div>
            </fieldset>
        </form>
    </div>
</div>
${ ui.includeFragment("ugandaemrpoc", "resultForm") }







