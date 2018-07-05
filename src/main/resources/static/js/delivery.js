$(document).ready(function(){
    $(".filter_date").datepicker({
        "dateFormat": "yy-mm-dd"
    });

    $(".record_form .date").datepicker({
        "dateFormat": "yy-mm-dd",
        "changeYear": true,
        "yearRange": '+0:+100'
    });

    $(".selectAll").click(function() {
        $(".checkItem").attr("checked", true);
    });

    $(".selectNone").click(function() {
        $(".checkItem").attr("checked", false);
    });

    $("#scanid").focus();

	$('.refreshCaptcha').click(function() {
        var captchaImg = $('#captcha_image');
		var url = captchaImg.attr('src');
		var queryStartPosition = url.indexOf('?');

		if (queryStartPosition > 0) {
			url = url.substring(0, queryStartPosition);
		}

		url = url + '?r=' + Math.random().toString(36).substring(2);
        captchaImg.attr("src", url);
	});

    $(':radio').change(function () {
        var elem = $(this);
        toggleCustomReproduction(elem);
        toggleReproductionForm();
    });
    toggleCustomReproduction();
    toggleReproductionForm();

    $('.with-sub-rows').on('click', 'tr', function () {
        $(this).nextUntil(':not(.sub-row)').toggleClass('hidden');
    });
});

function toggleCustomReproduction(elem) {
    var anyNonCustom = $('.on-any-non-custom');
    var allCustom = $('.on-all-custom');

    if (elem !== undefined) {
        var list = elem.closest('ul');
        var nonCustom = list.find('.on-non-custom');
        var custom = list.find('.on-custom');

        if (elem.hasClass('custom')) {
            nonCustom.hide();
            custom.show();
        }
        else {
            custom.hide();
            nonCustom.show();
        }
    }

    if ($(':radio.custom:checked').length > 0) {
        anyNonCustom.hide();
        allCustom.show();
    }
    else {
        allCustom.hide();
        anyNonCustom.show();
    }
}

function toggleReproductionForm() {
    var reproductionItemsSelect = $('.reproductionItemsSelect');
    if (reproductionItemsSelect.length > 0) {
        var noCheckedItems = (reproductionItemsSelect.find('input[type=radio]:checked').length === 0);
        $('.reproduction_form input').prop('disabled', noCheckedItems);
    }
}

function renumberHoldings() {
    var rows = $("table.holdings tr.holding");

    var newIdx = 0;
    rows.each(function(index) {
        var curIdx = $(this).attr('id').substr(7);
        var curPrefix = "holdings\\["+curIdx+"\\]\\.";
        var newPrefix = "holdings["+newIdx+"].";
        $(this).attr('id', "holding"+newIdx);

        $("#"+curPrefix+"signature").attr('id', newPrefix+"signature")
                                    .attr('name', newPrefix+"signature");
        $("#"+curPrefix+"usageRestriction").attr('id', newPrefix+"usageRestriction")
                                           .attr('name', newPrefix+"usageRestriction");
        newIdx++;
    });
}

function addNewHolding() {
    var rows = $("table.holdings tr.holding");
    var curIdx = -1;
    if (rows.size() > 0) {
        curIdx = parseInt(rows.last().attr('id').substr(7));
    }
    var newPrefix = "holdings["+(curIdx+1)+"].";
    var newHolding = $("#newHolding").clone();
    newHolding.removeClass("hidden");
    newHolding.addClass("holding");
    newHolding.attr('id', "holding"+(curIdx+1));
    newHolding.find("#holdings\\.new\\.signature").attr('id', newPrefix+"signature")
                                    .attr('name', newPrefix+"signature");
    newHolding.find("#holdings\\.new\\.usageRestriction").attr('id', newPrefix+"usageRestriction")
                                    .attr('name', newPrefix+"usageRestriction");

    newHolding.appendTo("table.holdings");
}

function addNewHoldingReservation(newHoldingReservation) {
    var rows = $("#holdingReservations .holdingReservations");
    var curIdx = -1;
    if (rows.size() > 0) {
        curIdx = parseInt(rows.last().attr('id').substr(18));
    }
    var newPrefix = "holdingReservations["+(curIdx+1)+"].";

    $('#newHoldingReservation .removeButton').clone().prependTo(newHoldingReservation);

    newHoldingReservation.addClass("holdingReservations");
    newHoldingReservation.attr('id', "holdingReservation"+(curIdx+1));
    newHoldingReservation.find(".holding").attr('id', newPrefix+"holding")
                                    .attr('name', newPrefix+"holding");
    var cl = newHoldingReservation.find(".commentlist");
    cl.removeClass("hidden");
    cl.find(".comment").attr('id', newPrefix+"comment")
                                    .attr('name', newPrefix+"comment");

    newHoldingReservation.find('.addButton').remove();
    newHoldingReservation.appendTo("#holdingReservations");
}

function removeNewHoldingReservation(newHoldingReservation) {
    $('#newHoldingReservation .addButton').clone().prependTo(newHoldingReservation);
    newHoldingReservation.attr('id', "");
    newHoldingReservation.find(".holding").attr('id', "")
                                     .attr('name', "");

    var cl = newHoldingReservation.find(".commentlist");
    cl.addClass("hidden");
    cl.find(".comment").attr('id', "").attr('name', "");

    newHoldingReservation.find('.removeButton').remove();
    newHoldingReservation.removeClass("holdingReservations");
    newHoldingReservation.appendTo("#holdingSearch");
}

function renumberHoldingReservations() {
    var rows = $("#holdingReservations .holdingReservations");

    var newIdx = 0;
    rows.each(function(index) {
        var curIdx = $(this).attr('id').substr(18);
        var curPrefix = "holdingReservations\\["+curIdx+"\\]\\.";
        var newPrefix = "holdingReservations["+newIdx+"].";
        $(this).attr('id', "holdingReservation"+newIdx);

        $("#"+curPrefix+"holding").attr('id', newPrefix+"holding")
                                    .attr('name', newPrefix+"holding");
        newIdx++;
    });
}

function addNewHoldingReproduction(newHoldingReproduction) {
    var rows = $("#holdingReproductions .holdingReproductions");
    var curIdx = -1;
    if (rows.size() > 0) {
        curIdx = parseInt(rows.last().attr('id').substr(19));
    }
    var newPrefix = "holdingReproductions["+(curIdx+1)+"].";

    $('#newHoldingReproduction .removeButton').clone().prependTo(newHoldingReproduction);

    newHoldingReproduction.addClass("holdingReproductions");
    newHoldingReproduction.attr('id', "holdingReproduction"+(curIdx+1));
    newHoldingReproduction.find(".holding").attr('id', newPrefix+"holding").attr('name', newPrefix+"holding");

    var hiddenItems = newHoldingReproduction.find("li.hidden");
    hiddenItems.removeClass("hidden");

    hiddenItems.find(".comment").attr('id', newPrefix+"comment").attr('name', newPrefix+"comment");
    hiddenItems.find(".standardOption").attr('id', newPrefix+"standardOption").attr('name', newPrefix+"standardOption");
    hiddenItems.find(".price").attr('id', newPrefix+"price").attr('name', newPrefix+"price");
    hiddenItems.find(".deliveryTime").attr('id', newPrefix+"deliveryTime").attr('name', newPrefix+"deliveryTime");
    hiddenItems.find(".customReproductionCustomer").attr('id', newPrefix+"customReproductionCustomer").attr('name', newPrefix+"customReproductionCustomer");
    hiddenItems.find(".customReproductionReply").attr('id', newPrefix+"customReproductionReply").attr('name', newPrefix+"customReproductionReply");

    newHoldingReproduction.find('.addButton').remove();
    newHoldingReproduction.appendTo("#holdingReproductions");
}

function removeNewHoldingReproduction(newHoldingReproduction) {
    $('#newHoldingReproduction .addButton').clone().prependTo(newHoldingReproduction);
    newHoldingReproduction.attr('id', "");
    newHoldingReproduction.find(".holding").attr('id', "").attr('name', "");

    newHoldingReproduction.find(".comment").attr('id', "").attr('name', "").closest('li').addClass('hidden');
    newHoldingReproduction.find(".standardOption").attr('id', "").attr('name', "").closest('li').addClass('hidden');
    newHoldingReproduction.find(".price").attr('id', "").attr('name', "").closest('li').addClass('hidden');
    newHoldingReproduction.find(".deliveryTime").attr('id', "").attr('name', "").closest('li').addClass('hidden');
    newHoldingReproduction.find(".customReproductionCustomer").attr('id', "").attr('name', "").closest('li').addClass('hidden');
    newHoldingReproduction.find(".customReproductionReply").attr('id', "").attr('name', "").closest('li').addClass('hidden');

    newHoldingReproduction.find('.removeButton').remove();
    newHoldingReproduction.removeClass("holdingReproductions");
    newHoldingReproduction.appendTo("#holdingSearch");
}

function renumberHoldingReproductions() {
    var rows = $("#holdingReproductions .holdingReproductions");

    var newIdx = 0;
    rows.each(function() {
        var curIdx = $(this).attr('id').substr(19);
        var curPrefix = "holdingReproductions\\["+curIdx+"\\]\\.";
        var newPrefix = "holdingReproductions["+newIdx+"].";

        var holdingReproduction = $(this);
        holdingReproduction.attr('id', "holdingReproduction"+newIdx);
        $("#"+curPrefix+"holding").attr('id', newPrefix+"holding") .attr('name', newPrefix+"holding");

        holdingReproduction.find(".comment").attr('id', newPrefix+"comment").attr('name', newPrefix+"comment");
        holdingReproduction.find(".standardOption").attr('id', newPrefix+"standardOption").attr('name', newPrefix+"standardOption");
        holdingReproduction.find(".price").attr('id', newPrefix+"price").attr('name', newPrefix+"price");
        holdingReproduction.find(".deliveryTime").attr('id', newPrefix+"deliveryTime").attr('name', newPrefix+"deliveryTime");
        holdingReproduction.find(".customReproductionCustomer").attr('id', newPrefix+"customReproductionCustomer").attr('name', newPrefix+"customReproductionCustomer");
        holdingReproduction.find(".customReproductionReply").attr('id', newPrefix+"customReproductionReply").attr('name', newPrefix+"customReproductionReply");

        newIdx++;
    });
}

function addNewStandardOption() {
    var rows = $("table.reproduction_standard_options tr.standard_option");
    var size = rows.size();
    var newPrefix = "options["+(size)+"].";
    var newStandardOption = $("#newStandardOption").clone();
    newStandardOption.removeAttr("id");
    newStandardOption.removeClass("hidden");
    newStandardOption.addClass("standard_option");
    newStandardOption.find("#new\\.optionNameNL")
        .attr('id', newPrefix+"optionNameNL")
        .attr('name', newPrefix+"optionNameNL");
    newStandardOption.find("#new\\.optionNameEN")
        .attr('id', newPrefix+"optionNameEN")
        .attr('name', newPrefix+"optionNameEN");
    newStandardOption.find("#new\\.optionDescriptionNL")
        .attr('id', newPrefix+"optionDescriptionNL")
        .attr('name', newPrefix+"optionDescriptionNL");
    newStandardOption.find("#new\\.optionDescriptionEN")
        .attr('id', newPrefix+"optionDescriptionEN")
        .attr('name', newPrefix+"optionDescriptionEN");
    newStandardOption.find("#new\\.materialType")
        .attr('id', newPrefix+"materialType")
        .attr('name', newPrefix+"materialType");
    newStandardOption.find("#new\\.level")
        .attr('id', newPrefix+"level")
        .attr('name', newPrefix+"level");
    newStandardOption.find("#new\\.price")
        .attr('id', newPrefix+"price")
        .attr('name', newPrefix+"price");
    newStandardOption.find("#new\\.deliveryTime")
        .attr('id', newPrefix+"deliveryTime")
        .attr('name', newPrefix+"deliveryTime");
    newStandardOption.find("#new\\.minNumberOfPages")
        .attr('id', newPrefix+"minNumberOfPages")
        .attr('name', newPrefix+"minNumberOfPages");
    newStandardOption.find("#new\\.maxNumberOfPages")
        .attr('id', newPrefix+"maxNumberOfPages")
        .attr('name', newPrefix+"maxNumberOfPages");
    newStandardOption.find("#new\\.enabled")
        .attr('id', newPrefix+"enabled")
        .attr('name', newPrefix+"enabled");
    newStandardOption.appendTo("table.reproduction_standard_options tbody");
}
