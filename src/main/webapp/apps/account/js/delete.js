hui.ui.listen({
  busy : false,
  $submit$confirmationForm : function(form) {
    if (this.busy) return;
    var values = form.getValues();
    if (hui.isBlank(values.username)) {
      form.focus();
      return;
    }
    if (hui.isBlank(values.password)) {
      form.focus();
      return;
    }
    this.busy = true;
    hui.ui.msg({busy: true, text: {da:'Sletter konto...',en:'Deleting account...'}})
    hui.ui.request({
      url : '/deleteAccount',
      parameters : {
        username : values.username,
        password : values.password
      },
      $success : function(response) {
        document.location.reload();
      },
      $failure : function(t) {
        var obj = hui.string.fromJSON(t.responseText);
        var msg = obj ? obj.message : {da:'Der skete en uventet fejl',en:'An unexpected error occured'};
        hui.ui.msg.fail({text: msg});
      },
      $finally : function() {
        this.busy = false;
      }.bind(this)
    })

  }
})