//-----------------------------------------------//
//
//                global
//
//-----------------------------------------------//
var Monitor_singleton;
var Monitor_version = "20120209-1111";
//-----------------------------------------------//
//
//                method
//
//-----------------------------------------------//
function initMonitor()
{
    var initFunction = new function()
	{
	    new Monitor();
	};
    YAHOO.util.Event.addListener( window, "load", initFunction);
}

function Monitor()
{
    Monitor_singleton = this;
    this.tabView = new YAHOO.widget.TabView();
    this.tabView.appendTo('container');

    //---------------------------------//
    //
    //           version
    //
    //---------------------------------//
    {
	var version = document.createElement('div');
	version.innerHTML = 
	    '<table width="100%"><tr>' +
	    '<td align=left><font color="#88888" size=2>version ' + Monitor_version + '</font></td>' +
	    '<td align=center><font color="#552288" size=3>Kevin</font></td>' +
	    '<td align=right><font color="#77777" size=3>.</font></td></tr></table>';
	this.tabView.appendChild( version);
    }

    //---------------------------------//
    //
    //           add jobMonitor
    //
    //---------------------------------//
    {
	var jobMonitor = new JobMonitor( this);
	var tab = new YAHOO.widget.Tab({
		label: 'Job Monitor',
		contentEl: jobMonitor.eMain,
		active: true
	    });
	tab.view = jobMonitor;
	this.tabView.addTab( tab);
    }

    //---------------------------------//
    //
    //           add errorMonitor
    //
    //---------------------------------//
    {
	var errorMonitor = new ErrorMonitor( this);
	var tab = new YAHOO.widget.Tab({
		label: 'Error Monitor',
		contentEl: errorMonitor.eMain,
		active: false
	    });
	tab.view = errorMonitor;
	this.tabView.addTab( tab);
    }


};

//EOF: qview.js
