// To scale schematic of Mymou holding system
// Grey coloured items are made of metal, whereas the rest is plastic

module cagebars() {
    for(i=[1:6]) {
        translate([20,-30+(i*56),5]) cylinder(h=280, r=8);
    };  
}
module hook() {
    translate([-44,0,-10]) {
        difference() {
            cube([44,20,10]);
            translate([2,-5,-2]){cube([40,30,10]);}
        }
    }
}

module padlockPin() {
    cube([13,36,25]);  // Front
    translate([14,0,0]){  // Gap for illustrative purposes
        cube([13,36,25]);  // Back
    }

    // Pin to slot through front of tablet holder
    translate([26,18,12.5]) {
        difference() {
            rotate([0,90,0]){
                cylinder(h=35, r=5);
            }
        // Hole in pin for padlock
            translate([25,7,0]) {
                rotate([90,0,0]){
                    cylinder(h=15, r=3);
                }  
            }  
        }
    } 
}

module juiceSpouts(radius) {
    for(i=[1:4]) {
        difference(){
            rotate([0,45,90]){
                translate([25+i*5,0,170]) {
                color([0.5,0.5,0.5])
                    cylinder(h = 300, r=radius, center = true);
                }
            } 
            translate([-15,220,190])cube([30,50,30]);
        }
    }
}

module juiceSpoutHolder(offset) {
    // Back
    difference() {
        translate([-12,0,0]) {
            cube([11,88,25]);  
        }
        // Space for juice spouts
        translate([0,offset,0]){
            rotate([45,0,0]) {
                translate([-8,-30,0]){
                    cube([9,80,25]);
                }
            }
        }
    }
    // Front
    translate([0,0,0]) {
        cube([20,9,25]);
    }
    translate([0,23,0]) {
        cube([20,40,25]);
    }
    translate([0,79,0]) {
        cube([20,9,25]);
    }
    translate([14,0,0]){
        cube([13,88,25]); 
    }
}

// Cage
color([0.7,0.7, 0.7])union(){
    cube([40,330,5]);
    cagebars();
    translate([7,0,5+280]) {cube([25,330,5]);};
    translate([0,0,5+280]) {cagebars();};
    translate([0,0,5+280+5+280]) {cube([40,330,5]);};
}

// Cage side 2
rotate([0,0,90]) {
    color([0.7,0.7,0.7])union(){
        cube([40,330,5]);
        cagebars();
        translate([7,0,5+280]) {cube([25,330,5]);};
        translate([0,0,5+280]) {cagebars();};
        translate([0,0,5+280+5+280]) {cube([40,330,5]);};
    }
}


translate([42,0,-2]){  // To move entire unit relative to cage

// Front plate
fpWidth = 330;
fpOpeningWidth = 230;
fpOpeningHeight = 370;
difference(){
cube([8,fpWidth,580]);
    // Space for window
    translate([-10,50,60])cube([30,fpOpeningWidth,fpOpeningHeight]);
    // Hole for padlock
    translate([-5,193,25]){
        rotate([0,90,0]){
            cylinder(h=30, r=6);
            };
        };
}


//Front plate hooks
color([0.5,0.5,0.5])union(){
    translate([0,50,580]) {hook();}
    translate([0,fpWidth-50,580]) {hook();}
}

//Pyramidal bit
pyrWidth = 270;
pyrHeight = 410;
extrudeRatio = 0.62;
translate([8,30+pyrWidth/2,250]){
    rotate([0,90,0]){
        difference() {
            linear_extrude(height = 190, scale = extrudeRatio) square([pyrHeight,pyrWidth], center=true);  
      translate([0,0,-5]) {linear_extrude(height = 200, scale = extrudeRatio) square([390,240], center=true);};
    }
}}




//Tablet holder
tabletH = 270;
tabletW = 188;
tabletD = 12;
translate([198,80,120]){  // Move entire tablet holder
difference() {cube([tabletD,tabletW,tabletH]);
    // Space for tablet
    translate([2,10,11-4.5]){cube([8.5,200,254.4]);}
    translate([-4.5,10,11]){cube([15,200,235]);}
    // Hole for charger
    translate([2,tabletW/2-10,tabletH-20]){cube([8,13,30]);}
    // Hole for locking pin
    translate([6,tabletW-18,tabletH-20]){cylinder(h=30, r=3);}
    //Holes to stop debris accumulation
    for(i=[1:11]) {
        translate([2,0+(i*15),-10]){cube([8,10,30]);
    }
    };
}
// Locking pin
translate([6,tabletW-18,tabletH]){
    cylinder(h=35, r=12.5);
    translate([0,0,-35]){
        color([0.5,0.5,0.5])cylinder(h=35, r=3);
        }
}
// Tablet itself
color([0,0,0])
    translate([2,11,11-4.5]){
        cube([8.5,155.3,254.4]);
        }
};}

// Clamp for padlock
color([0.5,0.5,0.5])union(){
    translate([20-13,175,10]){   
        difference() {
            padlockPin();
            // Holes for bolts to clamp unit onto cage bars
            translate([-2,4,12.5]) {
                rotate([0,90,0]){
                    cylinder(h=35, r=2);
                }
            }
            translate([-2,32,12.5]) {
                rotate([0,90,0]){
                    cylinder(h=35, r=2);
                }
            }
            //Cage bar hole
            translate([13,19,-5]) {
                rotate([0,0,0]){
                    cylinder(h=35, r=8);
                }
            }
        }
    }
}

//// Juice spouts
translate([4,-75,102])juiceSpouts(3);

// Juice spout holder
color([0.5,0.5,0.5])union(){
translate([7,123,259]){   
    difference() {
        juiceSpoutHolder(30);
        // Holes for bolts to clamp unit onto cage bars
        translate([-20,3,15]) {
            rotate([0,90,0]){
                cylinder(h=55, r=2);
            }
        }
        translate([-20,84,12.5]) {
            rotate([0,90,0]){
                cylinder(h=55, r=2);
            }
        }
        //Cage bar holes
        translate([13,15,-5]) {
            rotate([0,0,0]){
                cylinder(h=35, r=8);
            }
        }
        translate([13,15+56,-5]) {
            rotate([0,0,0]){
                cylinder(h=35, r=8);
            }
        }
    }
}

// Juice spout holder 2
color([0.5,0.5,0.5])union(){
translate([7,11,159]){   
    difference() {
        juiceSpoutHolder(42);
        // Holes for bolts to clamp unit onto cage bars
        translate([-20,3,15]) {
            rotate([0,90,0]){
                cylinder(h=55, r=2);
            }
        }
        translate([-20,84,12.5]) {
            rotate([0,90,0]){
                cylinder(h=55, r=2);
            }
        }
        //Cage bar holes
        translate([13,15,-5]) {
            rotate([0,0,0]){
                cylinder(h=35, r=8);
            }
        }
        translate([13,15+56,-5]) {
            rotate([0,0,0]){
                cylinder(h=35, r=8);
            }
        }
    }
}
}
}
