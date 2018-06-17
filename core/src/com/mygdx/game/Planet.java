package com.mygdx.game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.util.vmath;

public class Planet{
    public Array<Face> faces = new Array<Face>();
    public Array<Tile> tiles = new Array<Tile>();
    public Array<Vector3> points = new Array<Vector3>();
    private float scale;
  
    // create Planet constructor

    Planet() {}

	void generateIcosphere(float scale, int subdivisions){
		//hardcoded ico shit

		float phi = (float)((1.0f + Math.sqrt(5.0f))/2.0f);
		float u = 1.0f/(float)Math.sqrt(phi*phi + 1.0f);
		float v = phi*u;

		this.scale = scale;

		// Points are scaled x10 so the camera is more flexible

		points.addAll(
                new Vector3(0.0f,   +v,   +u),
                new Vector3(0.0f,   +v,   -u),
                new Vector3(0.0f,   -v,   +u),
                new Vector3(0.0f,   -v,   -u),
                new Vector3(+u,   0.0f,   +v),
                new Vector3(-u,   0.0f,   +v),
                new Vector3(+u,   0.0f,   -v),
                new Vector3(-u,   0.0f,   -v),
                new Vector3(+v,     +u, 0.0f),
                new Vector3(+v,     -u, 0.0f),
                new Vector3(-v,     +u, 0.0f),
                new Vector3(-v,     -u, 0.0f)
			);
		
		//for (Vector3 p: points){ p.scl(scale);}
		
	    // 20 faces
		faces.addAll(
                new Face(points.get(0), points.get( 8),  points.get( 1)),
                new Face(points.get(0), points.get( 5),  points.get( 4)),
                new Face(points.get(0), points.get(10),  points.get( 5)),
                new Face(points.get(0), points.get( 4),  points.get( 8)),
                new Face(points.get(0), points.get( 1),  points.get(10)),
                new Face(points.get(1), points.get( 8),  points.get( 6)),
                new Face(points.get(1), points.get( 6),  points.get( 7)),
                new Face(points.get(1), points.get( 7),  points.get(10)),
                new Face(points.get(2), points.get(11),  points.get( 3)),
                new Face(points.get(2), points.get( 9),  points.get( 4)),
                new Face(points.get(2), points.get( 4),  points.get( 5)),
                new Face(points.get(2), points.get( 3),  points.get( 9)),
                new Face(points.get(2), points.get( 5),  points.get(11)),
                new Face(points.get(3), points.get( 7),  points.get( 6)),
                new Face(points.get(3), points.get(11),  points.get( 7)),
                new Face(points.get(3), points.get( 6),  points.get( 9)),
                new Face(points.get(4), points.get( 9),  points.get( 8)),
                new Face(points.get(5), points.get(10),  points.get(11)),
                new Face(points.get(6), points.get( 8),  points.get( 9)),
                new Face(points.get(7), points.get(11),  points.get(10))
		 );

        subdivide(subdivisions);
    
        for (Vector3 p : points){
          p.nor().scl(scale);
        }
    
        setFaceNeighbors();
        //convertToDual();
        System.out.println("Faces: " + faces.size);
        System.out.println("Tiles: " + tiles.size);
	}

    public void setFaceNeighbors() {
        for(int i = 0; i < faces.size; i++) {
            for(int j = 0; j < faces.size; j++) {
                if(i == j) continue;
                if(faces.get(i).nbrs.size == 3) break;
                if(faces.get(i).testNeighbor(faces.get(j))) {
                    System.out.println(i + " and " + j + " are neighbors");
                    faces.get(i).addNbr(faces.get(j));
                    faces.get(j).addNbr(faces.get(i));
                }
            }
        }
    }
	
	void randomizeTopography(){
    	Face tempFc;
    	for (int i = 0; i < faces.size; i++){
			tempFc = faces.get(i);
			for (int j = 0; j < tempFc.pts.length; j++){
				tempFc.pts[j].scl(1.0f + 0.1f*(float)Math.random());
			}
		}
	}

    /* subdivides faces n times */
    public void subdivide(int degree) {
        for(int i = 0; i < degree; i++) {
            Array<Face> newFaces = new Array<Face>();
            for(Face face : faces) {
                Vector3 p0 = face.pts[0];
                Vector3 p1 = face.pts[1];
                Vector3 p2 = face.pts[2];

                Vector3 q0 = vmath.mid(p0, p1);
                Vector3 q1 = vmath.mid(p1, p2);
                Vector3 q2 = vmath.mid(p2, p0);

                points.addAll(q0, q1, q2);
                
                newFaces.addAll(
                        new Face(q0, q2, p0),
                        new Face(q1, q0, p1),
                        new Face(q2, q1, p2),
                        new Face(q0, q1, q2)
                );
                
            }
            // set faces = newFaces
            faces.clear();
            faces.ensureCapacity(newFaces.size - faces.size);
            faces.addAll(newFaces);
        }
    }

    public void convertToDual() {
        Array<Vector3> points = new Array<Vector3>();             // Array for Tile points
        Face curr;
        boolean isTile;
        for(Face face : faces) {
            curr = face;
            isTile = false;
            Vector3 p1 = curr.pts[0];                       // Tile centroid

            for(Tile tile : tiles) {                        // Check if tile with
                if(tile.centroid == p1) {                   //   that center exists
                    p1 = curr.pts[1];                       // If so, go to next point
                    break;
                }
            }
            for(Tile tile : tiles) {                        // Check if tile with
                if(tile.centroid == p1) {                   //   next center exists
                    isTile = true;                          // If so, every point on face
                    break;                                  //   is a tile, move on
                }
            }
            if(isTile) continue;

            do {
                points.add(curr.centroid);                  // add current centroid
                Vector3 p2 = curr.pts[getCwPt(curr, p1)];   // CCW point
//                System.out.println("Cn: " + p1.x + "," + p1.y + "," + p1.z);
//                System.out.println("Cw: " + p2.x + "," + p2.y + "," + p2.z);

                for (Face nbr : curr.nbrs) {                // find CCW neighbor
                    int count = 0;
                    for(int i = 0; i < nbr.pts.length; i++) {
                        if(nbr.pts[i] == p1 || nbr.pts[i] == p2) {
                            count++;
                        }
                    }
                    if(count == 2) {
                        curr = nbr;
//                        System.out.println("Nbr p1: " + nbr.pts[0].x + "," + nbr.pts[0].y + "," + nbr.pts[0].z);
//                        System.out.println("Nbr p2: " + nbr.pts[1].x + "," + nbr.pts[1].y + "," + nbr.pts[1].z);
//                        System.out.println("Nbr p3: " + nbr.pts[2].x + "," + nbr.pts[2].y + "," + nbr.pts[2].z);

                        break;
                    }
                }
            } while(curr != face);

            tiles.add(new Tile(p1, points));
            System.out.println("Tile added, centroid (" + p1.x + "," + p1.y + "," + p1.z
                    + "), " + points.size + " points");
            points.clear();                                 // clear points for next tile
        }
    }

    public int getCwPt(Face face, Vector3 TileCentroid) {
        int index = 0;

        // Find the index being used for the centroid
        for(int i = 0; i < face.pts.length; i++) {
            if(face.pts[i] == TileCentroid) {
                index = i;
                break;
            }
        }

        // Find the index of the next point CW from the centroid index
        if(index + 2 >= face.pts.length) {
            return index - 1;
        } else
            return index + 2;
    }
}
