function moveGroup(updateId, idLeft, idRight, orientation) {
    var leftList = document.getElementById(idLeft);
    var rightList = document.getElementById(idRight);
    var srcList = leftList;
    var desList = rightList;
    if (orientation == "left") {
        srcList = rightList;
        desList = leftList;
    }
    var arrOptions = new Array;
    if (srcList.options.length <= 0) {
        return;
    }
    if (srcList.selectedIndex < 0) {
        return;
    }
    for ( var i = 0; i < srcList.options.length; i++) {
        if (srcList.options[i].selected) {
            arrOptions.push(srcList.options[i]);
        }
    }
    for ( var i = 0; i < arrOptions.length; i++) {
        arrOptions[i].selected = "";
        desList.appendChild(arrOptions[i]);
    }
    updateSelectedGroup(updateId, rightList.options);
}

function moveGroupAll(updateId, idLeft, idRight, orientation) {
    var leftList = document.getElementById(idLeft);
    var rightList = document.getElementById(idRight);
    var srcList = leftList;
    var desList = rightList;
    if (orientation == "left") {
        srcList = rightList;
        desList = leftList;
    }
    var arrOptions = new Array;
    if (srcList.options.length <= 0) {
        return;
    }
    for ( var i = 0; i < srcList.options.length; i++) {
        arrOptions.push(srcList.options[i]);
    }
    for ( var i = 0; i < arrOptions.length; i++) {
        arrOptions[i].selected = "";
        desList.appendChild(arrOptions[i]);
    }
    updateSelectedGroup(updateId, rightList.options);
}

function updateSelectedGroup(updateId, options) {
    var updateList = document.getElementById(updateId);
    for ( var i = updateList.options.length; i >= 0 ; i--) {
        updateList.remove(i);
    }
    for ( var i = 0; i < options.length; i++) {
        var option = document.createElement("option");
        option.text = options[i].text;
        option.value = options[i].value;
        option.selected = "selected";
        try {
            // for IE earlier than version 8
            updateList.add(option, updateList.options[null]);
        } catch (e) {
            updateList.add(option, null);
        }
    }
}
