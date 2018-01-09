hui.ui.listen({
  userId : 0,

  $valueChanged$search : function() {
    list.resetState();
  },
  $open$list : function(row) {
    if (row.kind=='user') {
      this.loadUser(row);
    }
  },
  loadUser : function(row) {
    userFormula.reset();
    userEditor.show();
    userEditor.setBusy('Loading');
    this.userId = row.id;
    hui.ui.request({
      url : 'loadUser',
      parameters :{id : row.id},
      $object : function(user) {
        userFormula.setValues(user);
        userEditor.setBusy(false);
        userEditor.show();
        userFormula.focus();
      }
    })
  },
  $click$saveUser : function() {
    userEditor.setBusy('Saving');
    var values = userFormula.getValues();
    values.id = this.userId;
    hui.ui.request({
      url : 'saveUser',
      json : {user : values},
      $success : function(user) {
        list.refresh();
        this.userId = 0;
        userFormula.reset();
        userEditor.hide();
      },
      $failure : function() {
        hui.ui.msg.fail({text:'Failed to update'});
      },
      $finally : function() {
        userEditor.setBusy(false);
      }
    })
  },
  $click$cancelUser : function() {
    this.userId = 0;
    userFormula.reset();
    userEditor.hide();
  },
  $click$deleteUser : function() {
    userEditor.setBusy('Deleting');
    hui.ui.request({
      url : 'deleteUser',
      parameters :{id : this.userId},
      $success : function(user) {
        userFormula.reset();
        userEditor.hide();
        listSource.refresh();
      },
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to delete member'});
      },
      $finally : function() {
        userEditor.setBusy(false);
      }
    })
  },
  $select$selection : function() {
    list.resetState();
  },


  // Members...

  $click$newMember : function() {
    memberWindow.show();
    memberFormula.reset();
    memberFormula.focus();
  },

  $submit$memberFormula : function() {
    var values = memberFormula.getValues();
    memberWindow.setBusy('Creating');

    hui.ui.request({
      url : 'createMember',
      parameters : values,
      $success : function(user) {
        memberFormula.reset();
        memberWindow.hide();
        listSource.refresh();
      },
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to create member'});
      },
      $finally : function() {
        memberWindow.setBusy(false);
      }
    })

  }
});