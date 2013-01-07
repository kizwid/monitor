//TODO:embed current Dashoard ErrorMonitor in self-contained auto-refreshing unit (like jobMonitor)
//-----------------------------------------------//
//
//                global
//
//-----------------------------------------------//
var ErrorMonitor_sid = "sid" + Monitor_version + "-" + ( new Date()).getTime();

//-----------------------------------------------//
//
//                constructer
//
//-----------------------------------------------//
function ErrorMonitor( monitor)
{
    this.isUp = true;
    this.nEventMax = 100;
    this.nEventSize = 0;

    //
    //    create gui (read from service)
    //
    this.eMain = document.createElement( "div");
    this.eMain.view = this;

    //
    //    summary
    //
    {
	this.eSummary = document.createElement( "div");
	this.eMain.appendChild( this.eSummary);
    }

    //
    //    start long-poll
    //
    this.ajaxStart();
};

//-----------------------------------------------//
//
//                method
//
//-----------------------------------------------//
ErrorMonitor.prototype.close = function()
{
    this.isUp = false;
};

//-----------------------------------------------//
//
//                ajax method
//
//-----------------------------------------------//
ErrorMonitor.prototype.updateSummary = function( table)
{
    this.eSummary.innerHtml = table;
};
ErrorMonitor.prototype.ajaxSuccess = function( p_obj)
{
    if( !this.isUp)
	{
	    //alert( "errorMonitor already closed");
	    return;
	};

    var mapData = null;
    try
	{
	    //alert( p_obj.responseText);
	    eval( "mapData=" + p_obj.responseText);
	}
    catch( exp)
	{
	    //alert( "eval error: " + exp);
	    this.ajaxFailure();
	    return;
	};

    // summary
    this.updateSummary( p_obj.responseText);

    // next poll
    YAHOO.lang.later( 10000, this, this.ajaxStart, [], false);
};

ErrorMonitor.prototype.ajaxFailure = function( p_obj)
{
    //alert( 'ajaxFailure tId:' + p_obj.tId + ' status:' + p_obj.status);
    this.updateSummary( "trying to reconnect...");
    YAHOO.lang.later( 20000, this, this.ajaxStart, [], false);
};

ErrorMonitor.prototype.ajaxStart = function()
{
    sUrl = '/monitorApp/service/data?Action=GetErrors&Sid=' + ErrorMonitor_sid;
    var callback =
    {
	success: this.ajaxSuccess,
	failure: this.ajaxFailure,
	scope: this,
	cache: false,
	argument: [ 1, 2, 3]
    };

    var transaction = YAHOO.util.Connect.asyncRequest( 'GET', sUrl, callback, null);
};


// EOF: errorView.js
