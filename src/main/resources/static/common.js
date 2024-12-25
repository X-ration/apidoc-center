function ajaxGetJsonFull(url,paramObject,successFunction) {
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
        error: function (xhr) {
            console.error("请求出错", xhr.status, xhr.statusText);
        }
    });
}
function ajaxPostJsonFull(url,paramObject,csrfToken,successFunction) {
    $.ajax({
        url: url,
        type: 'POST',
        data: paramObject,
        beforeSend: function (request) {
            request.setRequestHeader('Content-Type','application/json;charset=UTF-8');
            request.setRequestHeader('X-CSRF-TOKEN',csrfToken);
        },
        success: successFunction,
        error: function (xhr) {
            console.error("请求出错", xhr.status, xhr.statusText);
        }
    });
}
function processErrorMsgItem(errorMsgItem, selector) {
    if(errorMsgItem != null) {
        selector.removeClass('is-valid');
        selector.addClass('is-invalid');
        selector.next().text(errorMsgItem);
    } else {
        selector.removeClass('is-invalid');
        selector.addClass('is-valid');
    }
}
function processErrorMsgItemOptional(errorMsgItem, selector) {
    if(selector.val() !== null && selector.val() !== '') {
        processErrorMsgItem(errorMsgItem, selector);
    }
}