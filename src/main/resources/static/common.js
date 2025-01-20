function ajaxGetJson(url,paramObject,successFunction) {
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
function ajaxPostJson(url,paramObject,csrfToken,successFunction) {
    ajaxPostJsonFull(url,paramObject,csrfToken,successFunction,ajax_common_error_function);
}
function ajaxPostJsonFull(url,paramObject,csrfToken,successFunction,errorFunction) {
    $.ajax({
        url: url,
        type: 'POST',
        data: paramObject,
        beforeSend: function (request) {
            request.setRequestHeader('Content-Type','application/json;charset=UTF-8');
            request.setRequestHeader('X-CSRF-TOKEN',csrfToken);
        },
        success: successFunction,
        error: errorFunction
    });
}
function ajaxPostFormFull(url,formData,csrfToken,successFunction,errorFunction) {
    $.ajax({
        type: 'POST',
        url: url,
        data: formData,
        processData: false,
        contentType: false,
        beforeSend: function (request) {
            request.setRequestHeader('X-CSRF-TOKEN',csrfToken);
        },
        success: successFunction,
        error: errorFunction
    });
}
function ajax_common_error_function(xhr) {
    console.error("请求出错", xhr.status, xhr.statusText);
}
function isBoolean(val) {
    return val === true || val === false;
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
function updateTagsBgColor(tagsinput, color) {
    var tags = tagsinput.prev().prev().find("span.tag");
    for(var i=0;i<tags.length;i++) {
        var tag = $(tags[i]);
        tag.attr('style','background-color: ' + color);
    }
}