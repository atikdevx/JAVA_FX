<div align="center">
  <br/>
  <h1 style="margin-bottom: 0; color: #FF007A; font-family: 'Helvetica Neue', sans-serif; font-size: 3.8em; font-weight: 900; letter-spacing: 4px; text-transform: uppercase; text-shadow: 2px 2px 8px rgba(255, 0, 122, 0.3);">
    ✦ Pika Plotter ✦
  </h1>
  <p style="font-family: 'Georgia', serif; font-style: italic; color: #7928CA; font-size: 1.5em; margin-top: 10px; font-weight: 600;">
    The Ultimate High-Performance Mathematical Visualization Engine
  </p>

  <p align="center" style="margin-top: 20px;">
    <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java"/>
    <img src="https://img.shields.io/badge/JavaFX_Graphics-1D3654?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX"/>
    <img src="https://img.shields.io/badge/Exp4j_Engine-FF007A?style=for-the-badge&logo=math&logoColor=white" alt="Exp4j"/>
    <img src="https://img.shields.io/badge/Maven_Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  </p>
</div>

<br/>

<blockquote style="border-left: 6px solid #FF007A; background-color: #FFF0F8; padding: 25px; border-radius: 8px; font-family: 'Segoe UI', sans-serif; color: #333; font-size: 1.15em; box-shadow: 0 4px 15px rgba(255, 0, 122, 0.1);">
  <strong>"Mathematics is the music of reason."</strong><br>
  <em>— James Joseph Sylvester</em>
</blockquote>

<br/>

## ✧ Architectural Overview & Libraries

At its core, **Pika Plotter** is driven by a powerful synergy of Java-based technologies, explicitly designed to handle intensive graphical computations without dropping frames:
* **JavaFX Canvas API:** Utilized for rapid, hardware-accelerated 2D and Polar drawing. Features double-buffering to eliminate screen tearing during parameter animations.
* **JavaFX 3D / Custom Projection:** Powers the spatial topological rendering, handling dynamic camera perspectives and depth-sorting matrices.
* **Exp4j (Extended):** The backbone mathematical parser. We heavily customized the base Exp4j library to parse dynamic parameters (`a`, `t`), handle fractional powers of negative bases, and process complex reciprocal trigonometric identities.

<hr style="border: 0; height: 2px; background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(121, 40, 202, 0.8), rgba(0, 0, 0, 0)); margin: 50px 0;">

## 📐 2D Cartesian Engine: Precision & Dynamics

<div align="center">
  <img src="Screenshot 2026-04-06 at 7.58.43 AM.jpg" width="48%" style="border-radius: 8px; box-shadow: 0 8px 16px rgba(0,0,0,0.2); margin-right: 2%;"/>
  <img src="Screenshot 2026-04-06 at 7.58.36 AM.jpg" width="48%" style="border-radius: 8px; box-shadow: 0 8px 16px rgba(0,0,0,0.2);"/>
  <p style="color: #666; font-size: 0.9em; margin-top: 10px;"><i>Light Mode: Perfect asymptote detection for <code>1/cos(x)</code> and <code>tan(x)</code>.</i></p>
</div>

<br/>

<div align="center">
  <img src="Screenshot 2026-04-07 at 10.52.13 AM.jpg" width="80%" style="border-radius: 12px; box-shadow: 0 10px 25px rgba(255,0,122,0.3); border: 2px solid #333;"/>
  <p style="color: #666; font-size: 0.9em; margin-top: 10px;"><i>Dark Mode: Complex animated heartbeat equation with a live parameter slider.</i></p>
</div>

### ✧ Features & Technical Implementation
* **Asymptote Circuit-Breaker:** Notice in the `1/cos(x)` and `tan(x)` graphs how there are no ugly vertical lines connecting the top to the bottom. The engine calculates the derivative and function domain in real-time; if it detects an infinite jump across a single pixel boundary, it "lifts the pen," ensuring mathematically flawless discontinuity rendering.
* **Real-Time Parameter Sliders (`a`, `b`, `c`):** As seen in the Dark Mode "Heart" graph, introducing an unknown variable like `a` auto-generates a sleek UI slider. Dragging it—or pressing the play/pause toggle—triggers a hardware-accelerated Canvas redraw at 60 FPS, turning static formulas into fluid motion.
* **Glass-Morphism UI & Theme Engine:** The draggable equation panel features a semi-transparent frosted glass effect. Instantly toggle between a crisp, legible Light Mode and a deep, neon-accented Dark Mode.

<hr style="border: 0; height: 1px; background: #ddd; margin: 50px 0;">

## 🧊 3D Surface Topology

<div align="center">
  <img src="Screenshot 2026-04-07 at 10.53.57 AM.jpg" width="80%" style="border-radius: 12px; box-shadow: 0 10px 25px rgba(0,201,255,0.3); border: 2px solid #eaeaea;"/>
  <p style="color: #666; font-size: 0.9em; margin-top: 10px;"><i>3D Mapping of <code>x^2 + y^2 = z</code> featuring spatial grid rendering and depth colorization.</i></p>
</div>

### ✧ Features & Technical Implementation
* **Multivariable Parsing:** The engine seamlessly isolates `x`, `y`, and `z` planes, evaluating <code>z = f(x,y)</code> over a generated 2D mesh grid and projecting it into 3D space.
* **Spatial Camera Controls:** Users can click and drag anywhere on the canvas to orbit around the topological surface. Scroll to zoom in on peaks and saddle points.
* **Dynamic Mesh & Axis Rendering:** Features a meticulously drawn 3D bounding box, perspective-accurate grid floors, and color-coded XYZ axes for perfect spatial orientation. The surface itself is rendered with vibrant magenta depth-shading to highlight curvature.
* **Multi-Curve Stacking:** The 3D UI allows for adding standard Surfaces, Implicit 3D equations, and 3D Kinematic curves <code>(x(t), y(t), z(t))</code> simultaneously.

<hr style="border: 0; height: 1px; background: #ddd; margin: 50px 0;">

## 🌀 Polar & Parametric Kinematics

<div align="center">
  <img src="Screenshot 2026-04-07 at 10.54.19 AM.jpg" width="80%" style="border-radius: 12px; box-shadow: 0 10px 25px rgba(121,40,202,0.4); border: 2px solid #333;"/>
  <p style="color: #666; font-size: 0.9em; margin-top: 10px;"><i>Polar Dark Mode: Highly complex, animated "Butterfly" curve mapping <code>r(t)</code>.</i></p>
</div>

### ✧ Features & Technical Implementation
* **Custom Radial Grid Engine:** When switching to Polar mode, the standard Cartesian grid is dynamically swapped for a beautiful concentric radial grid system, marked by angle measures and distance rings.
* **Parametric Time Sweeping `(t)`:** The parser is tuned to map massive, complex trigonometric outputs (like the butterfly curve shown) using extremely dense `t` step calculations. This ensures that chaotic, high-frequency oscillations remain perfectly smooth without jagged aliasing.
* **Live Temporal Animation:** Just like in 2D mode, the Polar engine supports dynamic variables. In the screenshot, the parameter `a` (between -0.4 and 0.4) is actively altering the frequency of the inner loops, with the "Pause" button active during live execution.

<hr style="border: 0; height: 2px; background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(255, 0, 122, 0.8), rgba(0, 0, 0, 0)); margin: 50px 0;">

## ✧ Installation & Build Instructions

<p style="font-size: 1.05em; color: #444;">Ensure you have <strong>Java 21+</strong> and <strong>Apache Maven</strong> installed. The <code>pom.xml</code> file automatically fetches all required JavaFX OS-specific binaries and the Exp4j core.</p>

<div style="background-color: #282A36; padding: 15px; border-radius: 8px; border-left: 5px solid #FF007A;">
<pre style="color: #F8F8F2; font-family: 'Fira Code', Consolas, monospace; margin: 0; font-size: 0.95em;">
<span style="color: #6272A4;"># 1. Clone the repository</span>
git clone https://github.com/yourusername/pika-plotter.git

<span style="color: #6272A4;"># 2. Navigate into the directory</span>
cd pika-plotter

<span style="color: #6272A4;"># 3. Clean and compile the application</span>
mvn clean compile

<span style="color: #6272A4;"># 4. Launch the Visualization Engine</span>
mvn javafx:run
</pre>
</div>

<br/>

<div align="center" style="margin-top: 40px; padding: 20px; background: linear-gradient(135deg, rgba(255,0,122,0.05), rgba(0,201,255,0.05)); border-radius: 50px;">
  <p style="font-family: 'Helvetica Neue', sans-serif; color: #666; font-size: 1.1em; margin: 0;">
    Designed and built with <span style="color: #FF007A;">❤️</span> and <strong>Advanced Calculus</strong>.
  </p>
</div>