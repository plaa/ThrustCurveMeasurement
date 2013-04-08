function time, filtered, raw, dt = plotfiltered(file)

data = load(file);

time = data(:,2);
time = (time - time(1))/1000000;

filtered = data(:,3);
if (length(data(1,:)) > 4)
  filtered = [filtered data(:,5)];
endif
if (length(data(1,:)) > 6)
  filtered = [filtered data(:,7)];
endif
if (length(data(1,:)) > 8)
  filtered = [filtered data(:,9)];
endif
if (length(data(1,:)) > 10)
  filtered = [filtered data(:,11)];
endif
if (length(data(1,:)) > 12)
  filtered = [filtered data(:,13)];
endif

raw = data(:,4);
if (length(data(1,:)) > 4)
  raw = [raw data(:,6)];
endif
if (length(data(1,:)) > 6)
  raw = [raw data(:,8)];
endif
if (length(data(1,:)) > 8)
  raw = [raw data(:,10)];
endif
if (length(data(1,:)) > 10)
  raw = [raw data(:,12)];
endif
if (length(data(1,:)) > 12)
  raw = [raw data(:,14)];
endif

dt = time(2:length(time)) - time(1:length(time)-1);
dt = [dt; dt(length(dt))];

plot(time, filtered);
xlabel("Time / s");

