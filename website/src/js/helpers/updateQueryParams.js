GW.Helpers = GW.Helpers || {};
GW.Helpers.updateQueryParams = function(param, value) {
   var re = new RegExp("([?&])" + param + "=.*?(&|#|$)(.*)", "gi"),
       hash,
       url = window.location.href;

    if (re.test(url)) {
        if (! _.isUndefined(value) && ! _.isNull(value))
            return url.replace(re, '$1' + param + "=" + value + '$2$3');
        else {
            hash = url.split('#');
            url = hash[0].replace(re, '$1$3').replace(/(&|\?)$/, '');
            if (! _.isUndefined(hash[1]) && ! _.isNull(hash[1])) {
                url += '#' + hash[1];
            }
            return url;
        }
    }
    else {
        if (! _.isUndefined(value) && ! _.isNull(value)) {
            var separator = url.indexOf('?') !== -1 ? '&' : '?';
            hash = url.split('#');
            url = hash[0] + separator + param + '=' + value;
            if (! _.isUndefined(hash[1]) && ! _.isNull(hash[1])) {
                url += '#' + hash[1];
            }
            return url;
        }
        else
            return url;
    }
};
