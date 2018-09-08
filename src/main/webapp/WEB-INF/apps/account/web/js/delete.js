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
    hui.ui.msg({busy: true, text: 'Deleting account...'})
    hui.ui.request({
      url : '/deleteAccount',
      parameters : {
        username : values.username,
        password : values.password
      },
      $success : function(response) {
        document.location.reload();
      },
      $failure : function() {
        hui.ui.msg.fail({text:'It failed!'});
      },
      $finally : function() {
        this.busy = false;
      }.bind(this)
    })

  }
})