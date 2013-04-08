## Filter data using a butterworth filter.
##
##   output = butterfilter(input, Fs, Fc, n, shift)
##
##   input  is the data to be filtered
##   Fs     is the sampling frequency
##   Fc     is the cutoff frequency
##   n      is the order of the butterworth filter
##   shift  (optional) is the number of samples to shift the output to the left
##
## If output is not provided this function will plot the data instead
##
function output = bufferfilter(input, Fs, Fc, n, shift)

% From http://www.tty1.net/blog/2009-09-12-filters-with-gnu-octave_en.html

% Nyquist frequency, [Hz]
% The Nyquist frequency is half your sampling frequency.
Fnyq = Fs/2;

% Create a first-order Butterworth low pass
[b,a]=butter(n, Fc/Fnyq);

% Apply the filter to the input signal and plot input and output.
out=filter(b,a,input);
if (nargin >= 5)
  out=out(shift:length(out));
endif

if (nargout == 0)
  tin = (0:(length(input)-1)) / Fs;
  tout = (0:(length(out)-1)) / Fs;
  plot(tin, input, tout, out, 'r')
  xlabel("Time / s");
else
  output = out;
endif

