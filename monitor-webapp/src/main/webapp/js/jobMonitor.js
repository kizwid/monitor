//-----------------------------------------------//
//
//                global
//
//-----------------------------------------------//
var Monitor_version = "20120127-1747";
var JobMonitor_sid = "sid" + Monitor_version + "-" + ( new Date()).getTime();

//-----------------------------------------------//
//
//                constructor
//
//-----------------------------------------------//
function JobMonitor( monitor)
{
    this.isUp = true;
    this.nEventMax = 1;
    this.nEventSize = 0;

    //
    //    create gui
    //
    this.eMain = document.createElement( "div");
    this.eMain.view = this;

    //
    //    charts version 2.7.0
    //
    {
	YAHOO.widget.Chart.SWFURL = "http://yui.yahooapis.com/2.9.0/build/charts/assets/charts.swf";

	// create data source
	/*
	var testData =
	    [
	     { jobSet: "jobSet001", completed: 852, running: 2359, queueing: 2393 },
	     { jobSet: "jobSet002", completed: 776, running: 8952, queueing: 6620 },
	     ];
	*/
	this.chartDataSource = new YAHOO.util.DataSource([]);
	this.chartDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	this.chartDataSource.responseSchema = { fields: [ "jobSet", "completed", "running", "queueing" ] };

	// define series definition
	this.seriesDef =
	    [
	     {
		 xField: "completed",
		 displayName: "Completed jobs",
         style: {
           size: 10
         }
	     },
	     {
		 xField: "running",
		 displayName: "Running Jobs",
         style: {
           size: 10
         }
	     },
	     {
		 xField: "queueing",
		 displayName: "Queueing Jobs",
         style: {
           size: 10
	     }
	     }
	     }
	     ];

	// create an axis
	//used to format x axis
	var number2Jobs = function( value )
	    {
		return YAHOO.util.Number.format(Number(value), {suffix: " jobs", thousandsSeparator: ","});
	    }
	
	//Numeric Axis for our Jobs
	this.jobsAxis = new YAHOO.widget.NumericAxis();
	this.jobsAxis.stackingEnabled = true;
	this.jobsAxis.labelFunction = number2Jobs;

	// place holder div
	this.bChartCreated = false;
	this.eChart = document.createElement( "div");
	this.eChart.id="jobMonitorChart";
	this.eMain.appendChild( this.eChart);
    }

    //
    //    news
    //
    {
	this.eNews = document.createElement( "div");
	this.eMain.appendChild( this.eNews);
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
JobMonitor.prototype.close = function()
{
    this.isUp = false;
};

//-----------------------------------------------//
//
//                ajax method
//
//-----------------------------------------------//
JobMonitor.prototype.addLine = function( p_sLine)
{
    this.nEventSize++;
    if( this.nEventSize > this.nEventMax)
	{
	    var eFirstChild = this.eNews.firstChild;
	    if( eFirstChild != null) this.eNews.removeChild( eFirstChild);
	}
    var divEvent = document.createElement( 'div');
    divEvent.className = "jobMonitorAlert";
    divEvent.appendChild( document.createTextNode( p_sLine));
    divEvent.appendChild( document.createElement( 'br'));
    this.eNews.appendChild( divEvent);
};

JobMonitor.prototype.ajaxSuccess = function( p_obj)
{
    if( !this.isUp) 
	{
	    //alert( "JobMonitor already closed");
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

    // chart
    {
	if( !this.bChartCreated)
	    {
		// Create a StackedBarChart
		this.mychart = new YAHOO.widget.StackedBarChart( this.eChart, this.chartDataSource,
								 {
								     series: this.seriesDef,
								     yField: "jobSet",
								     xAxis: this.jobsAxis,
								     //only needed for flash player express install
								     expressInstall: "assets/expressinstall.swf"
								 });


        //chart-click
        this.mychart.on("itemClickEvent", handleClick);

		this.bChartCreated = true;
	    };


	this.chartDataSource.liveData = mapData.aChartData;
	this.mychart.set("dataSource", this.chartDataSource);
	this.mychart.refreshData();

    }


    // news
    this.addLine( "[" + mapData.sTimeStamp + "] " + mapData.sEvent);

    // next poll
    YAHOO.lang.later( 20000, this, this.ajaxStart, [], false);
};

JobMonitor.prototype.ajaxFailure = function( p_obj)
{
    //alert( 'ajaxFailure tId:' + p_obj.tId + ' status:' + p_obj.status);
    this.addLine( "trying to reconnect...");
    YAHOO.lang.later( 20000, this, this.ajaxStart, [], false);
};

JobMonitor.prototype.ajaxStart = function()
{
    sUrl = '/monitorApp/service/data?Action=GetJobStats&Sid=' + JobMonitor_sid;
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


function handleClick(eventObj)
{
    var str = "Event type: " + eventObj.type;
    for(var i in eventObj.item)
    {
    str += "\n" + i + ": " + eventObj.item[i];
    }
    str += "\n" + "Index: " + eventObj.index;
    str += "\n" + "Series Index: " + eventObj.seriesIndex;
    str += "\n" + "x: " + eventObj.x;
    str += "\n" + "y: " + eventObj.y;
    alert(str);
}


// EOF: jobMonitor.js
