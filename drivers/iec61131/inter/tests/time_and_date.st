
BEFORE := T#4h44m;
AFTER := T#15s;
DATE1:= DT#2013-06-21-20:00:00;
CURRENT := LocalDT();
COMPARE1:=FALSE;
COMPARE2:=FALSE;

COMPARE1:=CURRENT > DATE1 - BEFORE + AFTER;
COMPARE2:=CURRENT > (DATE1 - BEFORE) + AFTER;

