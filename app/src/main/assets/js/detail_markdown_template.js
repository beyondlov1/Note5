function searchAndScrollTo(target, inputLength) {
    var result = '';
    var node = [];

    function getNodes(obj) {
        var nodes = obj.childNodes;
        for (var key in nodes) {
            if (typeof nodes[key] === "object" && nodes[key].nodeType === 1) {

                if (nodes[key].childNodes.length > 1) {
                    getNodes(nodes[key]);
                } else {
                    if (nodes[key].tagName != "SCRIPT") {
                        node[node.length] = nodes[key];
                    }
                }
            }
        }
    }

    var obj = document.getElementsByTagName('body')[0];
    getNodes(obj);
    var pElement = node;
    for (var i = 0; i < pElement.length; i++) {
        result = result + pElement[i].innerHTML;
    }
    var tmp = "";
    for (var i = pElement.length - 1; i >= 0; i--) {
        for (var j = pElement[i].innerHTML.length - 1; j >= 0; j--) {
            tmp = pElement[i].innerHTML[j].replace(/\p{Punct}*|\s*|\r|\n|/g, "") + tmp;
            if (tmp == target) {
                if (j >= inputLength) {
                    var newpElement = insert_flg(pElement[i].innerHTML, "</a>", j);
                    newpElement = insert_flg(newpElement, "<a id='found' style='margin:0 0px;'>", j - inputLength);
                    pElement[i].innerHTML = newpElement;
                } else if (inputLength <= 0) {
                    var newpElement = insert_flg(pElement[i].innerHTML, "<a id='found' style='margin:0 0px;'></a>", j);
                    pElement[i].innerHTML = newpElement;
                } else {
                    if (i >= 1) {
                        var newpElement = insert_flg(pElement[i - 1].innerHTML, "</a>", pElement[i - 1].innerHTML.length);
                        newpElement = insert_flg(newpElement, "<a id='found' style='margin:0 0px;'>", pElement[i - 1].innerHTML.length - inputLength);
                        pElement[i - 1].innerHTML = newpElement;
                    }

                }
                break;
            }
        }

    }
    $.scrollTo(document.getElementById('found').offsetTop - 40, 600);
    //            window.scrollTo(0, document.getElementById('found').offsetTop-10);
    return document.getElementById('found').offsetTop;
}

function insert_flg(str, flg, sn) {
    var newstr = "";
    for (var i = 0; i < str.length; i++) {
        if (i == sn) {
            newstr = newstr + flg + str[i];
        } else {
            newstr = newstr + str[i];
        }
    }
    return newstr;
}
