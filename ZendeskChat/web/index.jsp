<%-- 
    Document   : index
    Created on : Dec 6, 2012, 11:26:05 AM
    Author     : kaiwangubiquiti
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="css/page.css" rel="stylesheet" type="text/css">
        <link href="css/fivestar.css" rel="stylesheet" type="text/css">
        <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">
        <script src="http://code.jquery.com/jquery-latest.js"></script>
        <script src="bootstrap/js/bootstrap.min.js"></script>
        <title>JSP Page</title>
    </head>
    <body>
        <div id="survey">
            <div class="rating">
                <input type="radio" id="star5" name="rating" value="5" /><label for="star5" title="Very Good"></label>
                <input type="radio" id="star4" name="rating" value="4"/><label for="star4" title="Good"></label>
                <input type="radio" id="star3" name="rating" value="3" /><label for="star3" title="OK"></label>
                <input type="radio" id="star2" name="rating" value="2" /><label for="star2" title="Bad"></label>
                <input type="radio" id="star1" name="rating" value="1" /><label for="star1" title="Very Bad"></label>
            </div>
            <br/>
            <div class="control-group">
                <textarea name="StarComment" rows="8" id="starComment"></textarea>
                <input type="hidden" name="ticketid">
            </div>
            <div class="control-group">
                <div class="controls">
                    <button class="btn btn-primary" id="survey-submit">submit</button>
                    <span id="close"><a href="#">close</a></span>
                </div>
            </div>
        </div>
        <div id="chat"></div>

        <script type="text/javascript" src="js/zenbox.js"></script>
        <style type="text/css" media="screen, projection">
            @import url(css/zenbox.css);
        </style>
        <script type="text/javascript">
            if (typeof(Zenbox) !== "undefined") {
                Zenbox.init({
                    dropboxID:   "20122978",
                    url:         "https://ubnt.zendesk.com",
                    tabID:       "Chat",
                    tabColor:    "black",
                    tabPosition: "Right"
                });
            }
        </script>
    </body>
</html>
