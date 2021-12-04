# pdf-omr-pmerge
_Listen_ to PDF sheet music! This project helps convert PDFs to a playable .midi or a complete MuseScore .mscx file. The script is intended to be a super quick and easy tool, see `Usage` [below](#usage). At the core, it acts as a wrapper to Myriad's PDF to Music (p2mp), [listed below](#more-stuff), and it uses a custom script ([mxcat](https://github.com/kaisubr/mxcat)) to merge XML files.
<p align="center">
  <img src="media/sample_out.png" style="text-align: center" />
  </br>
  <i>Playable output extracted from original <a href="https://github.com/kaisubr/pdf-omr-pmerge/raw/master/media/original.pdf">PDF file</a></i>
</p>

## Usage
Usage is very simple. You just need to provide a PDF file. 

```bash
./pmerge.sh "path/to/myfile.pdf"
```

The outputs will be a complete MIDI (`result.mid`, which can even be visualized with bemuse) and MuseScore (`result.mscx`, along with a compressed counterpart) in the same directory (these can be opened with software such as MuseScore or Finale). If needed, MuseScore 3.4.2 is provided in the binary release.

<!--
## Releases
Download from the release pane, and run `install.sh`:
```bash
./install.sh
```
-->

## Building from source
Dependencies are provided, apart from `pdftk`. Optionally install `qpdf` if you are dealing with encrypted PDFs.

```bash
git clone "https://github.com/kaisubr/pdf-omr-pmerge.git"
sudo apt-get install pdftk
chmod +x pdftomusicpro-1.7.1d.0.run
./pdftomusicpro-1.7.1d.0.run
which p2mp

# MuseScore
wget "https://github.com/musescore/MuseScore/releases/download/v3.4.2/MuseScore-3.4.2-x86_64.AppImage"

# Install qpdf, optional
sudo apt-get install qpdf 
```

## Known issues and solutions
* Password-protected PDFs:
     - They can be decrypted using qpdf. Edit the script to provide a password with `qpdf -password=<password> -decrypt input.pdf decrypted.pdf`

## More stuff
Please use this script for private use only, not commercial or third-party use, and follow licenses provided by authors of dependencies.

Thanks to dependencies: mxcat (custom script), p2mp (PDFToMusic, pdftomusicpro-1.7.1d.0.run, included but must be executed, will add to /usr/bin automatically); pdftk (typically pre-installed)
 
Debug mode: Edit the first line of this file to: `#!/bin/bash -x`. You can also disable cleanup by removing the last few lines of this script.

<!-- cd musicxml && clear && echo -e "\n\n\n\n\n" && ls -1 && cd .. && ls | grep "mid" && echo -e "\n\n\n\n\n\n\n\n" -->
