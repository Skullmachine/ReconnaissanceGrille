\NeedsTeXFormat{LaTeX2e}

%----- Identification -----
\ProvidesClass{EPURapport}[2008/01/09 - EPURapport document class]

%----- Required Packages -----
\RequirePackage[latin1]{inputenc}
\RequirePackage{ifthen}
\RequirePackage{fancyhdr}

%----- Options -----
\newboolean{BDeuxPages}

\DeclareOption{twoside}{
	\PassOptionsToClass{twoside}{report}%
	\setboolean{BDeuxPages}{true}%
}

\DeclareOption*{\PassOptionsToClass{\CurrentOption}{report}}

%----- Execution of Options -----
\ProcessOptions

%----- Initial Class -----
\LoadClass[11pt,a4paper]{report}
\RequirePackage[francais]{babel}
\RequirePackage{makeidx}
\RequirePackage[pdftex]{graphicx}
\RequirePackage{url}
\RequirePackage{xcolor}
\RequirePackage{comment}
\RequirePackage{lastpage}
% hyperref pour avoir des liens clickables dans le pdf
\RequirePackage[]{hyperref}
% fp, pour le calcul de nombre de pages � ajouter � la fin
\RequirePackage{fp}

%----- Dimensions -----
\setlength{\evensidemargin}{2cm}
\setlength{\oddsidemargin}{2cm}
\setlength{\textheight}{\paperheight}
\addtolength{\textheight}{-5.7cm} % 24cm
\setlength{\textwidth}{\paperwidth}
\addtolength{\textwidth}{-4cm} % 17cm
\setlength{\topmargin}{0.5cm}
\setlength{\headheight}{2cm}
\setlength{\headsep}{0.5cm}
\setlength{\voffset}{-2.54cm}
\setlength{\hoffset}{-2.54cm}
\setlength{\footskip}{1.5cm}

%----- Personnal config -----
\def\notableofcontents{%
	\renewcommand{\tableofcontents}{}
}

\def\nolistoffigures{%
	\renewcommand{\listoffigures}{}
}

%\def\nolistoftables{%
%	\renewcommand{\listoftables}{}
%}

\newcommand{\addextratables}[1]{%
	\def\extratables{#1}
}

%Definition Tuteur 

\def\supervisorstitle{Tuteur}

\newcommand{\setsupervisorstitle}[1]{%
	\def\supervisorstitle{#1}
}

\def\nosupervisors{%
	\renewcommand{\supervisorsdisplay}{}
}

%Definition Apprenti 

\def\authorstitle{Apprenti}

\newcommand{\setauthorstitle}[1]{%
	\def\authorstitle{#1}
}

%----- HyperRef configuration -----
\hypersetup{
	colorlinks=true,
	urlcolor=blue,
	breaklinks=true,
	bookmarksopen=true,
	bookmarksnumbered=true,
	plainpages=false,
	pdfdisplaydoctitle=true
}

%----- Urls -----
\urlstyle{sf}
\newcommand{\email}[1]{
	\href{mailto:#1}{#1}
}

%----- Babel numbers -----
%1ere, 2eme
\renewcommand{\iere}{\up{\lowercase{\`ere}}~\xspace}
\renewcommand{\ieme}{\up{\lowercase{\`eme}}~\xspace}

%----- Getting informations -----
%%%--- Document ---
\newcommand{\thedocument}[3]{%
	\def\thedocumenttype{#1}% Rapport de projet, Rapport de stage de fin d'ann�e, projet ing�nieur ann�e X
	\def\thedocumentlongtitle{#2}
	\def\thedocumentshorttitle{#3}
	
	%% Title and subject in document properties
	\hypersetup{
	 pdftitle={\thedocumentlongtitle},
	 pdfsubject={\thedocumenttype : \thedocumentshorttitle}
	}
}

%%%--- Grade ---
%(Parcours des \'Ecoles d'Ing\'enieur Polytech, 1\iere ann\'e, D\'epartement Informatique, 2\ieme ann\'e...)
\newcommand{\grade}[1]{%
	\def\thegrade{#1}
}

%%%--- Authors ---
\newcommand{\authors}[6]{%
	% We have at least one author
	\def\thefirstauthorname{#1}	\def\thefirstauthormail{#2}
	\ifthenelse{\NOT\equal{#3}{} }% if nb authors >= 2
	{
		\def\thesecondauthorname{#3}	\def\thesecondauthormail{#4}
		\ifthenelse{\NOT\equal{#5}{} }% if nb authors = 3
		{
			\def\thethirdauthorname{#5}	\def\thethirdauthormail{#6}
		}{}
	}{}
	
	%% Authors in document properties %%
	\ifthenelse{\NOT\isundefined{\thethirdauthorname}}
	{% If nb authors = 3
		\hypersetup{
			pdfauthor={\thefirstauthorname\ <\thefirstauthormail>, \thesecondauthorname\ <\thesecondauthormail>, \thethirdauthorname\ <\thethirdauthormail>}
		}
	}{}
	{% Else if nb authors <= 2
		\ifthenelse{\NOT\isundefined{\thesecondauthorname}}
		{
			\hypersetup{
				pdfauthor={\thefirstauthorname\ <\thefirstauthormail>, \thesecondauthorname\ <\thesecondauthormail>}
			}
		}
		{% Else there is at least one author defined
			\hypersetup{
				pdfauthor={\thefirstauthorname\ <\thefirstauthormail>}
			}
		}
	}
}

%%%--- Supervisors ---
\newcommand{\supervisors}[7]{%
	\def\thefirstsupervisorname{#1}	\def\thefirstsupervisormail{#2}
	\ifthenelse{ \NOT\equal{#3}{} }%if nb supervisors >= 2
	{
		\def\thesecondsupervisorname{#3}	\def\thesecondsupervisormail{#4}
		\ifthenelse{ \NOT\equal{#5}{} }% if nb supervisors = 3
		{
			\def\thethirdsupervisorname{#5}	\def\thethirdsupervisormail{#6}
		}{}
	}{}
	\def\thesupervisorsarecomingfrom{#7}
}

%%%--- Abstracts and Keywords ---
\newcommand{\abstracts}[4]{%
	\def\thefrenchabstract{#1}
	\def\thefrenchkeywords{#2}
	\def\theenglishabstract{#3}
	\def\theenglishkeywords{#4}
	
	%%French Keywords in document properties
	\hypersetup{
	 pdfkeywords={\thefrenchkeywords}
	}
}

%----- Displaying authors -----
\newcommand{\authorsdisplay}{
	\begin{minipage}[t]{7cm}
		\underline{\authorstitle\ifthenelse{ \NOT\isundefined{\thesecondauthorname} }{s}{}} :\\
		\textbf{\thefirstauthorname}\\
		\email{\thefirstauthormail}
		\ifthenelse{ \NOT\isundefined{\thesecondauthorname} }%if nb authors >= 2
		{
			\\\textbf{\thesecondauthorname}\\
			\email{\thesecondauthormail}
			\ifthenelse{ \NOT\isundefined{\thethirdauthorname} }%if nb authors =3
			{
				\\\textbf{\thethirdauthorname}\\
				\email{\thethirdauthormail}
			}{}
		}{}
	\end{minipage}
}

%----- Displaying supervisor -----
\newcommand{\supervisorsdisplay}{
	\begin{minipage}[t]{7cm}
		\begin{flushright}
			\underline{\supervisorstitle\ifthenelse{ \NOT\isundefined{\thesecondsupervisorname} }{s}{}} :\\
			\textbf{\thefirstsupervisorname}\\
			\email{\thefirstsupervisormail}
			\ifthenelse{ \NOT\isundefined{\thesecondsupervisorname} }%if nb supervisors >= 2
			{
				\\\textbf{\thesecondsupervisorname}\\
				\email{\thesecondsupervisormail}
				\ifthenelse{ \NOT\isundefined{\thethirdsupervisorname} }%if nb supervisors =3
				{
					\\\textbf{\thethirdsupervisorname}\\
					\email{\thethirdsupervisormail}
				}{}
			}{}
			\\\thesupervisorsarecomingfrom
		\end{flushright}
	\end{minipage}
}

%----- First Page -----
\newcommand{\thefirstpage}{%

\thispagestyle{empty}
	\sffamily
	%\setcounter{page}{1}
	\begin{center}
		\includegraphics[height=4cm]{Logo_Polytech_Tours_et_DI_et_UFR_2.pdf}\\
		\vspace{1cm}
		{
			{\'Ecole Polytechnique de l'Universit\'e de Tours}\\
			64, Avenue Jean Portalis\\
			37200 TOURS, FRANCE\\
			T\'el. +33 (0)2 47 36 14 14\\
			Fax  +33 (0)2 47 36 14 22\\
			\href{http://www.polytech.univ-tours.fr}{\texttt{www.polytech.univ-tours.fr}}
		}

		\vspace{2cm}
		
		{\LARGE \textbf{\thegrade}\\~\\\textbf{\thedocumenttype}\\}
		
		\vspace{2.5cm}
		
		{\Huge \textbf{\thedocumentlongtitle}}
		
		\vfill
		
		\authorsdisplay \hfill\hfill \supervisorsdisplay
		
		\vspace{1.5cm}
		
		{\small Version du \today}
	\end{center}
	
	\ifthenelse{\boolean{BDeuxPages}}%
	{% On commence sur une page droite
		\newpage~\thispagestyle{empty}\newpage
	}
	
	% Affichage des pages en chiffres romains pour les pages informatives
	\renewcommand{\thepage}{\Roman{page}}
	
	% Affichage des tables et listes habituelles
	\tableofcontents
	\listoffigures
	%\listoftables
	% Et des non habituelles
	\ifthenelse{\NOT\isundefined{\extratables}}
	{\extratables}{}
	\newpage
	
	\makeindex
	
	% Affichage des pages en chiffres arabes pour le corps du document
	\renewcommand{\thepage}{\arabic{page}}
}

%----- Last Page -----
\newcommand{\thelastpage}{%
	\printindex
	
	% Si on est en mode twosides, on va chercher � avoir un
	% nombre de pages multiple de 4
	\ifthenelse{\boolean{BDeuxPages}}
	{%
		\FPset{\DivB}{4}
	
		\FPset{\DivA}{\thepage}
	
		%% Decomposition du nb de pages en A = BQ+R
		%% Compiler avec --shell-escape pour avoir le debug (debug unix)
		%% C'est le R qui nous interesse
		%%% Example : DivA = 26 pages
		\immediate\write18{printf "Nombre de pages : \DivA"}
	
		\FPdiv{\tmpDivQ}{\DivA}{\DivB} %% /4 ou /2
		\immediate\write18{printf "Division : \tmpDivQ"}
		%%% tmpDivQ = 6.75
	
		\FPtrunc{\DivQ}{\tmpDivQ}{0} %% Nb de paquets complets de 4 pages
		\immediate\write18{printf "Nombre de paquets de 4 pages : \DivQ"}
		%%% DivQ = 6
	
		\FPmul{\DivBQ}{\DivQ}{\DivB}
		\immediate\write18{printf "Nombre de pages en paquet : \DivBQ"}
		%%% DivBQ = 24
	
		\FPsub{\DivR}{\DivA}{\DivBQ}
		\FPtrunc{\DivR}{\DivR}{0}
		\immediate\write18{printf "Nombre de pages hors paquet : \DivR"}
		%%% DivR = 27-24 = 3
	
		\FPifzero{\DivR}
		{
			\FPset{\DivB}{0}
			\immediate\write18{printf "On ne rajoutera pas de pages : \DivR, \DivB"}
		}\else {
			\immediate\write18{printf "On rajoutera des pages"}
		}
	
		\FPsub{\Delta}{\DivB}{\DivR}
		\FPtrunc{\Delta}{\Delta}{0}
		\immediate\write18{printf "Nombre de pages a ajouter : \Delta"}
		%%% Delta = 4-3 = 1
	
		\loop
			\FPsub{\Delta}{\Delta}{1}
			\FPifpos{\Delta}{
				\immediate\write18{printf "Ajout d'une page \Delta"}
				\newpage~\thispagestyle{empty}
			}
		\repeat
	}
	{}
	
	% En mode "twoside", la derni�re page sera impaire, il faut donc 
	% intercepter l'ajout d'une page suppl�mentaire.
	\renewcommand\clearemptydoublepage{}
	\chapter*{\thedocumentlongtitle}
	\thispagestyle{empty}
	\begin{center}
		{\large\thegrade}\\~\\
		{\large\thedocumenttype}\\~\\
	\end{center}

	\paragraph{R\'esum\'e:} \thefrenchabstract
	\paragraph{Mots clefs:} \thefrenchkeywords
	%\paragraph{Abstract :} \theenglishabstract
	

%\paragraph{Keywords :} \theenglishkeywords
	\vspace{1cm}
	\hrule
	\vfill
	
	\authorsdisplay \hfill\hfill \supervisorsdisplay
}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
%----- Header/footer Format -----
\pagestyle{fancy}

% Affichage du titre du chapitre
%% \chaptername = "Chapitre"
%% \thechapter = numéro du chapitre
%% #1 = nom du chapitre
%% --> leftmark
\renewcommand{\chaptermark}[1]{%
	\markboth{\chaptername\ \thechapter. \ #1}{}}

% Affichage du titre de la section
%% #1 = nom de la section
%% --> rightmark
\renewcommand{\sectionmark}[1]{\markright{#1}}

% Largeur de l'en-t�te
% On enl�ve 3cm afin de pouvoir placer lo logo
\newlength{\headersize}
\setlength{\headersize}{\textwidth}
\addtolength{\headersize}{-3cm}

% Logo utilis� dans l'en-t�te
\newsavebox{\EPUlogo}
\sbox{\EPUlogo}{\includegraphics[height=1.4cm]{Logo_Polytech_Tours_et_DI_et_UFR_2.pdf}}

\ifthenelse{\boolean{BDeuxPages}}{% Twosides mode
	
	\fancyhead[LE]{% En-t�te, champ gauche, page paire (gauche)
		\begin{minipage}[c]{3cm}\usebox{\EPUlogo}\end{minipage}%
		\begin{minipage}{\headersize}%
		{%
			\bfseries\leftmark%
		}%
		\end{minipage}%
	}
	
	\fancyhead[RO]{% En-t�te, champ droit, page impaire (droite)
		\begin{minipage}{\headersize}%
		{%
			\begin{flushright}\bfseries\rightmark\end{flushright}%
		}%
		\end{minipage}%
		\begin{minipage}[c]{3.1cm}\hfill\usebox{\EPUlogo}\end{minipage}%
	}
	
	% En t�te, champ droit page paire (gauche), et champ gauche page impaire (droite) 
	\fancyhead[RE,LO]{}
	
	% Pied de page
	\fancyfoot[LE,RO]{\bfseries\thepage}
	\fancyfoot[RE,LO]{\bfseries\thedocumentshorttitle}
	%% Pour les pages avec titre
	\fancypagestyle{plain}{%
		\fancyfoot[LE,RO]{\bfseries\thepage}
		\fancyfoot[RE,LO]{}
	}
}{% Oneside mode
	% En-t�te, champ gauche
	\fancyhead[L]{%
		\begin{minipage}{3.1cm}\usebox{\EPUlogo}\end{minipage}%
		\begin{minipage}{\headersize}%
		{%
			\bfseries\leftmark\\%
			\rightmark%
		}%
		\end{minipage}%
	}
	
	% En-t�te, champ gauche
	\fancyhead[R]{}
	
	% Pied de page
	\fancyfoot[R]{\bfseries\thepage}
	\fancyfoot[L]{\bfseries\thedocumentshorttitle}
	%% Page avec titre
	\fancypagestyle{plain}{%
		\fancyfoot[R]{\bfseries\thepage}
		\fancyfoot[L]{}
	}
}

%% R�glages valables dans tous les cas
% Rien au centre du pied de page
\fancyfoot[C]{}

% Epaisseur des lignes de s�paration
\renewcommand{\headrulewidth}{0pt}
\renewcommand{\footrulewidth}{0.2pt}

% Pages avec en-t�te (d�but de chapitre, table des mati�res...)
\fancypagestyle{plain}{%
	\fancyhead{}% Rien dans l'en-t�te
}

% D�finit une ligne horizontale de 2px d'�paisseur
% sur 3/4 de la page. Utilisée pour souligner le 
% titre d'un chapitre.
\newcommand{\semirule}{\hbox to \hsize{\hfill
    \vrule height 2pt width.75\hsize
    \hfill}}

% Redifinition de l'en-t�te d'un chapitre g�n�r�
% par la commande \chapter{}.
\def\@makechapterhead#1{%
  \begingroup
  \fontsize{\@xivpt}{18}\bfseries\centering
    \ifnum\c@secnumdepth>\m@ne
      \leavevmode \hskip-\leftskip
      \rlap{\vbox to\z@{\vss
	  \centerline{\LARGE\mdseries
	      \MakeUppercase{\@xp{\chaptername}\enspace\thechapter}}
	  \vskip 3pc}}\hskip\leftskip\fi
     \Huge\textsf{#1}\par\semirule\vspace{1cm} \endgroup
  \skip@34\p@ \advance\skip@-\normalbaselineskip
  \vskip\skip@ }

% Redifinition de l'en-t�te d'un chapitre g�n�r�
% par la commande \chapter*{}. 
\def\@makeschapterhead#1{%
  \begingroup
  \fontsize{\@xivpt}{18}\bfseries\centering
  \Huge\textsf{#1}\par\semirule\vspace{1cm} \endgroup
  \skip@34\p@ \advance\skip@-\normalbaselineskip
  \vskip\skip@ }

% Fork de l'option cleardoublepage pour l'option "twoside". Permet
% d'ajouter une page avec un \thispagestyle "empty" au lieu de "plain"
\newcommand\clearemptydoublepage{\clearpage\if@twoside \ifodd\c@page\else
    \hbox{}\thispagestyle{empty}\newpage\if@twocolumn\hbox{}\thispagestyle{empty}\newpage\fi\fi\fi}

% Red�finition de la commande \chapter. Si on est en mode "twoside", 
% La page ajout�e afin de commencer un chapitre � droite sera empty
% et non "plain".
\renewcommand\chapter{\if@openright\clearemptydoublepage\else\clearpage\fi
                    \thispagestyle{plain}%
                    \global\@topnum\z@
                    \@afterindentfalse
                    \secdef\@chapter\@schapter}

\newcommand{\introduction}
{
	\chapter*{Introduction}
	\addcontentsline{toc}{chapter}{\numberline{}Introduction}
	\markboth{Introduction}{}
}

\newcommand{\conclusion}
{
	\chapter*{Conclusion}
	\addcontentsline{toc}{chapter}{\numberline{}Conclusion}
	\markboth{Conclusion}{}
}

\newcommand{\annexe}{
	\appendix
	\renewcommand\chaptername{\appendixname}
}

% Gestion de la premi�re et derni�re page.
\AtBeginDocument{\thefirstpage}
\AtEndDocument{\thelastpage}
