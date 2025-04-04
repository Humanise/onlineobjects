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
  $select$list : function(selection) {
    infoIcon.setEnabled(!!selection);
    passwordResetIcon.setEnabled(!!selection);
    emailConfirmationIcon.setEnabled(!!selection);
    checkHealth.setEnabled(!!selection);
    logOut.setEnabled(!!selection);
  },
  $click$infoIcon : function() {
    var row = list.getFirstSelection();
    if (row) {
      this.loadUser(row);
    }
  },
  $click$passwordResetIcon : function() {
    var row = list.getFirstSelection();
    if (row) {
      hui.ui.msg({busy:true, text: 'Sending...'});
      hui.ui.request({
        url : 'sendPasswordReset',
        parameters :{id : row.id},
        $success : function(user) {
          hui.ui.msg.success({text:'It is on its way'})
        },
        $failure : function() {
          hui.ui.msg.fail({text:'It failed'})
        }
      });
    }
  },
  $click$emailConfirmationIcon : function() {
    var row = list.getFirstSelection();
    if (row) {
      hui.ui.msg({busy:true, text: 'Sending...'});
      hui.ui.request({
        url : 'sendEmailConfirmation',
        parameters :{id : row.id},
        $success : function(user) {
          hui.ui.msg.success({text:'It is on its way'})
        },
        $failure : function() {
          hui.ui.msg.fail({text:'It failed'})
        }
      });
    }
  },
  $click$checkHealth : function() {
    var row = list.getFirstSelection();
    if (row) {
      hui.ui.request({
        message: {start:'Sending...', success: 'Scheduled!'},
        url : 'checkHealth',
        parameters :{id : row.id},
        $failure : function() {
          hui.ui.msg.fail({text:'It failed'})
        }
      });
    }
  },
  $click$logOut : function() {
    var row = list.getFirstSelection();
    if (row) {
      hui.ui.request({
        message: {start:'Logging out...', success: 'Done!'},
        url : 'logUserOut',
        parameters :{id : row.id},
        $success : function() {
          listObjectsSource.refresh();
        },
        $failure : function() {
          hui.ui.msg.fail({text:'It failed'})
        }
      });
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

  // Clients...
  _clientId: null,
  
  $open$objectsList : function(row) {
    if (row.kind == 'client') {
      this._editClient({id: row.id});
    }
  },
  _editClient : function(params) {
    this._clientId = params.id;
    clientPanel.show();
  },
  $click$deleteClient: function() {
    clientPanel.setBusy(true);
    hui.ui.request({
      url: 'deleteClient',
      parameters : {id: this._clientId},
      $success : function() {
        listObjectsSource.refresh();
        clientPanel.hide();
      },
      $finally : function() {
        clientPanel.setBusy(false);
      },
      message : {start:'Deleting...', success: 'Deleted'}
    });
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