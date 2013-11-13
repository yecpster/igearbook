function verifyModerationCheckedTopics()
{
	var f = document.formModeration.topic_id;
	if (f.length == undefined)	 {
		if (f.checked) {
			return true;
		}
	}
	for (var i = 0; i < f.length; i++) {
		if (f[i].checked) {
			return true;
		}
	}
	alert("${I18n.getMessage("Moderation.SelectTopics")}");
	return false;
}

function validateModerationSetSticky()
{
	var status = verifyModerationCheckedTopics()
		&& confirm("确定要置顶吗");
	
	if (status) {
		document.formModeration.log_type.value = "4";
		document.formModeration.log_description.value = "主题置顶";
	}

	return status;
}

function validateModerationSetGood()
{
    var status = verifyModerationCheckedTopics()
        && confirm("确定要设置精华吗");
    
    if (status) {
        document.formModeration.log_type.value = "5";
        document.formModeration.log_description.value = "主题设置精华";
    }

    return status;
}
function validateModerationUnSetSticky()
{
    var status = verifyModerationCheckedTopics()
        && confirm("确定要取消置顶吗");
    
    if (status) {
        document.formModeration.log_type.value = "6";
        document.formModeration.log_description.value = "主题取消置顶";
    }

    return status;
}

function validateModerationUnSetGood()
{
    var status = verifyModerationCheckedTopics()
        && confirm("确定要取消精华吗");
    
    if (status) {
        document.formModeration.log_type.value = "7";
        document.formModeration.log_description.value = "主题取消精华";
    }

    return status;
}

function validateModerationDelete()
{
    var status = verifyModerationCheckedTopics()
        && confirm("${I18n.getMessage("Moderation.ConfirmDelete")}")
        && askModerationReason();

    if (status) {
        document.formModeration.log_type.value = "1";
    }

    return status;
}

function lockUnlock() 
{
	var status = verifyModerationCheckedTopics() && askModerationReason();
	
	if (status) {
		document.formModeration.log_type.value = "3";
	}

	return status;
}

function askModerationReason() 
{
	var message = prompt("${I18n.getMessage("ModerationLog.changeReason")}");

	if (message == null || message == "") {
		alert("${I18n.getMessage("ModerationLog.reasonIsEmpty")}");
		return false;
	}
	else {
		document.formModeration.log_description.value = message;
	}

	return true;
}

var oldClasses = {}

function changeTrClass(from, trIndex)
{
	var tr = from.parentNode.parentNode;
	trIndex = trIndex.toString();

	if (from.checked) {
		tr.className = "moderation_highlight";
		oldClasses[trIndex] = new Array();

		for (var i = 0; i < tr.childNodes.length; i++) {
			var node = tr.childNodes[i];

			if (node.nodeName.toUpperCase() == "TD") {
				oldClasses[trIndex].push(node.className);
				node.className = "";
			}
		}
	}
	else {
		tr.className = "";

		for (var i = tr.childNodes.length - 1; i >= 0; i--) {
			var node = tr.childNodes[i];

			if (node.nodeName.toUpperCase() == "TD") {
				node.className = oldClasses[trIndex].pop();
			}
		}
	}	
}