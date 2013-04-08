function F, freq = plotfft(data, frequency)

% Normalize the data
data = data - mean(data);
F = abs(fft(data));
F = F(1:round(length(F)/2));
freq = (0:(length(F)-1))/length(F)*frequency/2;
maxf = prctile(F, 99.9);
if (length(maxf)>1)
  maxf = prctile(F',99.9);
endif
plot(freq,F);

axis([0 frequency/2 0 maxf]);
xlabel("Frequency / Hz");

