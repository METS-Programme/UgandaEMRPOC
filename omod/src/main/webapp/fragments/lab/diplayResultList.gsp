<script>
    function displayLabResult(labQueueList) {
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
            orderedTestsRows += "<a title=\"Print Results\" onclick='printresult(" + element.orderId + "," + element.patientId + ")'><i class=\"icon-print small\"></i></a>";
            orderedTestsRows += "</td>";
            orderedTestsRows += "</tr>";
            referedTests += orderedTestsRows;
        });

        jq("#lab-results-tab").html("");
        if (referedTests.length > 0) {
            jq("#lab-results-tab").append(tableHeader + referedTests + tableFooter);
        } else {
            jq("#lab-results-tab").append("No Data ");
        }
    }
</script>