// IE6+SSL fix courtesy of http://www.tribalogic.net/
;
(function(window) {
    // Zendesk Feedback Tab version 2.5
    // Most of the assets are from version 2.1. There is a slight difference
    // in the markup inside the feedback tab and some changes to the CSS for
    // that markup.
    var document = window.document,
    urlWithScheme = /^([a-zA-Z]+:)?\/\//,
    settings = {
        url:            null, // required
        dropboxID:      null, // required
        tabID:          "support",
        tabText:        "Support", // most browsers will use the tabID image rather than this text
        tabColor:       "#000000",

        // the remaining settings are optional and listed here so users of the library know what they can configure:
        assetHost:      "//assets.zendesk.com",
        tabPosition:    'Left',     // or 'Right'
        hide_tab:       false,      // if true, don't display the tab after initialization
        request_subject:      null,  // pre-populate the ticket submission form subject
        request_description:  null,  //  "     "      "     "      "        "   description
        requester_name:       null,  //  "     "      "     "      "        "   user name
        requester_email:      null   //  "     "      "     "      "        "   user email
    },
    // references to elements on the page:
    tab,
    survey,
    survey_submit,
    close_link,
    overlay,
    container,
    closeButton,
    iframe,
    scrim;

    function attempt(fn) {
        try {
            return fn();
        } catch(e) {
            if (window.console && window.console.log && window.console.log.apply) {
                window.console.log("Zenbox Error: ", e);
            }
        }
    }

    function bindEvent(element, eventName, callback) {
        if (element && element.addEventListener) {
            element.addEventListener(eventName, callback, false);
        } else if (element && element.attachEvent) {
            element.attachEvent('on' + eventName, callback);
        }
    }

    function prependSchemeIfNecessary(url) {
        if (url && !(urlWithScheme.test(url))) {
            return document.location.protocol + '//' + url;
        }
        else {
            return url;
        }
    }

    // must be called after the DOM is loaded
    function createElements() {
        survey = document.getElementById('survey');
        survey_submit = document.getElementById('survey-submit');
        close_link = document.getElementById('close');
        tab = document.createElement('div');
        tab.setAttribute('id', 'zenbox_tab');
        tab.setAttribute('href', '#');
        tab.style.display = 'none';
        document.getElementById("chat").appendChild(tab);
        //    document.body.appendChild(tab);

        overlay = document.createElement('div');
        overlay.setAttribute('id', 'zenbox_overlay');
        overlay.style.display = 'none';
        overlay.innerHTML = '<div id="zenbox_container">' +
        '  <div class="zenbox_header"><img id="zenbox_close"></div>' +
        '  <iframe id="zenbox_body" frameborder="0" scrolling="auto" allowTransparency="true"></iframe>' +
        '</div>' +
        '<div id="zenbox_scrim">&nbsp;</div>';
        document.body.appendChild(overlay);

        container   = document.getElementById('zenbox_container');
        closeButton = document.getElementById('zenbox_close');
        iframe      = document.getElementById('zenbox_body');
        scrim       = document.getElementById('zenbox_scrim');

        bindEvent(survey_submit, 'click', function(){
            resetSurvey();
            tab.style.display='block';
            survey.style.display='none';
        });
        bindEvent(close_link, 'click', function(){ 
            resetSurvey();
            tab.style.display='block'; 
            survey.style.display='none'; 
        });
        bindEvent(tab,          'click', function() {
            tab.style.display='none';
            survey.style.display='none';
//            survey.style.display='block';
            var showSurvey = setInterval(function(){
                survey.style.display='block';
            }, 5000);
            clearInterval(showSurvey);
            window.Zenbox.show();
        });
        bindEvent(closeButton,  'click', function() {
            tab.style.display='none';
            survey.style.display='block';
            window.Zenbox.hide();
        });
        bindEvent(scrim,        'click', function() {
            tab.style.display='none';
            survey.style.display='block';
            window.Zenbox.hide();
        });
    }
    
    function resetSurvey(){
        // reset radio and comment box
        for(var i=1; i<=5; i++){
            var radio = document.getElementById('star'+i);
            radio.checked = false;
        }
        document.getElementById('starComment').value = "";
    }

    function configure(options) {
        var prop;
        for (prop in options) {
            if (options.hasOwnProperty(prop)) {
                if (prop === 'url' || prop === 'assetHost') {
                    settings[prop] = prependSchemeIfNecessary(options[prop]);
                } else {
                    settings[prop] = options[prop];
                }
            }
        }
    }

    function setupIE(position) {
        if (position === "left") {
            tab.style.filter = "progid:DXImageTransform.Microsoft.BasicImage(rotation=3)";          /* IE 6,7 */
            tab.style["-ms-filter"] = '"progid:DXImageTransform.Microsoft.BasicImage(rotation=3)"'; /* IE 8 */
            tab.style.left = "-15px";
        } else {
            tab.style.filter = "progid:DXImageTransform.Microsoft.BasicImage(rotation=1)";          /* IE 6,7 */
            tab.style["-ms-filter"] = '"progid:DXImageTransform.Microsoft.BasicImage(rotation=1)"'; /* IE 8 */
            var offset;
            if (navigator.userAgent.match(/MSIE 8/)) {
                offset = '-87px';
            } else if (navigator.userAgent.match(/MSIE 7/)) {
                offset = '-17px';
            } else {
                offset = '-50px';
            }
            tab.style.right = offset;
        }
    }

    function updateTab() {
        if (settings.hide_tab) {
            tab.style.display = 'none';
        } else {
            tab.setAttribute('title', settings.tabID);
            tab.setAttribute('class', 'ZenboxTab' + settings.tabPosition);
            tab.setAttribute('className', 'ZenboxTab' + settings.tabPosition);
            tab.innerHTML = '<div id="feedback_tab_text">' + settings.tabID + '</div>';
            tab.style.backgroundColor = settings.tabColor;
            tab.style.display         = 'block';
            var offset = Math.ceil(-(tab.offsetWidth - (tab.offsetHeight - 30)) / 2) + "px";
            var position = (tab.className === "ZenboxTabRight") ? "right" : "left";
            tab.style[position] = offset;

            if (navigator.userAgent.match(/MSIE [^9]/)) {
                setupIE(position);
            }
        }
    }

    function cancelEvent(e) {
        var event = e || window.event || {};
        event.cancelBubble = true;
        event.returnValue = false;
        event.stopPropagation && event.stopPropagation();
        event.preventDefault && event.preventDefault();
        return false;
    }

    function getDocHeight(){
        return Math.max(
            Math.max(document.body.scrollHeight, document.documentElement.scrollHeight),
            Math.max(document.body.offsetHeight, document.documentElement.offsetHeight),
            Math.max(document.body.clientHeight, document.documentElement.clientHeight)
            );
    }

    function getScrollOffsets(){
        return {
            left: window.pageXOffset || document.documentElement.scrollLeft || document.body.scrollLeft,
            top:  window.pageYOffset || document.documentElement.scrollTop  || document.body.scrollTop
        };
    }

    function closeButtonURL() {
        return settings.assetHost + '/external/zenbox/images/close_big.png';
    }

    // get the URL for the "loading" page to be used as iframe src while
    // loading the Dropbox.
    function loadingURL() {
        return settings.assetHost + '/external/zenbox/v2.1/loading.html';
    }

    function dropboxURL() {
        var url = settings.url + "/account/dropboxes/" + settings.dropboxID + '?x=1';
        if (settings.request_subject)     {
            url += '&subject='      + settings.request_subject;
        }
        if (settings.request_description) {
            url += '&description='  + settings.request_description;
        }
        if (settings.requester_name)      {
            url += '&name='         + settings.requester_name;
        }
        if (settings.requester_email)     {
            url += '&email='        + settings.requester_email;
        }
        return url;
    }

    function initialize(options) {
        if (!tab) {
            createElements();
        }
        configure(options);
        updateTab();
        closeButton.src = closeButtonURL();
        iframe.src = loadingURL();

        window.addEventListener('message', function(e) {
            if (e.data === 'hideZenbox') {
                hide();
            }
        }, false);
    }

    function show(evt) {
        iframe.src = dropboxURL();
        overlay.style.height = scrim.style.height = getDocHeight() + 'px';
        container.style.top = getScrollOffsets().top + 50 + 'px';
        overlay.style.display = "block";
        return cancelEvent(evt);
    }

    function hide(evt) {
        overlay.style.display = 'none';
        iframe.src = loadingURL();
        return cancelEvent(evt);
    }

    var Zenbox = {

        /*
        PUBLIC API

        Methods in the public API can be used as callbacks or as direct calls. As such,
        they will always reference "Zenbox" instead of "this." Each one is wrapped
        in a try/catch block to ensure that including Zenbox doesn't break the page.
    */

        /*
     *  Build and render the Zenbox tab and build the frame for the Zenbox overlay,
     *  but do not display it.
     *  @see settings for options
     *  @param {Object} options
     */
        init: function(options) {
            attempt(function() {
                return initialize(options);
            });
        },

        /*
     * Alias for #init.
     */
        update: function(options) {
            attempt(function() {
                return initialize(options);
            });
        },

        /*
     *  Render the Zenbox. Alias for #show.
     *  @see #show
     */
        render: function(evt) {
            attempt(function() {
                return show(evt);
            });
        },

        /*
     *  Show the Zenbox. Aliased as #render.
     *  @params {Event} event the DOM event that caused the show; optional
     *  @return {false} false always, in case users want to bind it to an
     *                  onclick or other event and want to prevent default behavior.
     */
        show: function(evt) {
            attempt(function() {
                return show(evt);
            });
        },

        /*
     *  Hide the Zenbox.
    <% if dropbox.tab_custom_image_url.present? %>
      tabImageURL: "<%= dropbox.tab_custom_image_url %>",
    <% end %>
      *  @params {Event} event the DOM event that caused the show; optional
      *  @return {false} false always, in case users want to bind it to an
      *                  onclick or other event and want to prevent default behavior.
      */
        hide: function (evt){
            attempt(function() {
                return hide(evt);
            });
        }
    };

    bindEvent(window, 'load', function() {
        if (window.zenbox_params) {
            Zenbox.init(window.zenbox_params);
        }
    });

    window.Zenbox = Zenbox;

}(this.window || this));


