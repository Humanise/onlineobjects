hui.control({
  '#name' : 'questions',

  'answerQuestion.click!' : function() {
    var item = appController.getCurrentItem();
    var url = '/app/question/answer?id=' + item.id;
    var output = hui.find('.js-question-intel');
    output.innerText = 'Let me think...';
    output.style.display = '';
    oo.intelligence.stream({url: url, $html: (s) => {
      output.innerHTML = s;
    }})
  }
});