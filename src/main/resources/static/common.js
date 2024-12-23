function ajaxGetJsonFull(url,paramObject,successFunction,errorFunction) {
    var paramEntries = Object.entries(paramObject);
    for(var i=0;i<paramEntries.length;i++) {
        var paramEntry = paramEntries[i];
        var key = paramEntry[0], value = paramEntry[1];
        if(i === 0) {
            url = url + '?';
        }
        url = url + key + '=' + value + "&";
    }
    if(paramEntries.length > 0) {
        url = url.substring(0, url.length - 1);
    }
    $.ajax({
        url: url,
        type: 'GET',
        dataType: 'json',
        success: successFunction,
        error: errorFunction
    });
}
function processErrorMsgItem(errorMsgItem, selector) {
    if(errorMsgItem != null) {
        selector.addClass('is-invalid');
        selector.next().text(errorMsgItem);
    } else {
        selector.addClass('is-valid');
    }
}
function processErrorMsgItemOptional(errorMsgItem, selector) {
    if(selector.val() !== null && selector.val() !== '') {
        processErrorMsgItem(errorMsgItem, selector);
    }
}