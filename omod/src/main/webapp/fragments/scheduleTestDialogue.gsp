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
                                    <%
                                            }
                                        }
                                    %>
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