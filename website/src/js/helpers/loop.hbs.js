Handlebars.registerHelper('loop', function(count, options) {
  var out = "",
      data,
      position = 1,
      index = 0;

  if (options.data) {
    data = Handlebars.createFrame(options.data);
  }

  while (count--) {
    if (data) {
      data.position = position++;
      data.index = index;
    }

    out = out + options.fn(count, { data: data });
    index++;
  }

  return out;
});
