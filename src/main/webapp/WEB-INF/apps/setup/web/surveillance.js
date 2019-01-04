hui.ui.listen({
  live : false,

  $ready : function() {

  },
  $click$refresh : function() {
    listSource.refresh();
  },
  $valueChanged$live : function(value) {
    this.live = value;
    if (value) {
      this.$sourceIsNotBusy$listSource();
    }
  },
  $sourceIsNotBusy$listSource : function() {
    if (this.live) {
      window.setTimeout(function() {
        listSource.refresh();
      },2000);
    }
  },
  $click$sendReport : function() {    
    hui.ui.request({
      url: '/sendSurveillanceReport',
      message : {start:'Sending', success:'Sent!'},
      $failure : function() {
        hui.ui.msg.fail({text:'It did not work'})
      }
    })
  }
});