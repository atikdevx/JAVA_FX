<div align="center">
  <br/>
  <h1 style="margin-bottom: 0; color: #FF007A; font-family: 'Helvetica Neue', sans-serif; font-size: 3.5em; font-weight: 900; letter-spacing: 3px; text-transform: uppercase;">
    ✦ Equation Plotter ✦
  </h1>
  <p style="font-family: 'Georgia', serif; font-style: italic; color: #7928CA; font-size: 1.4em; margin-top: 10px; font-weight: 600;">
    An Elegant, High-Performance Mathematical Visualization Engine Built with JavaFX
  </p>

  <p align="center" style="margin-top: 20px;">
    <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java"/>
    <img src="https://img.shields.io/badge/JavaFX-1D3654?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX"/>
    <img src="https://img.shields.io/badge/Exp4j_Engine-FF007A?style=for-the-badge&logo=math&logoColor=white" alt="Exp4j"/>
    <img src="https://img.shields.io/badge/Maven_Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  </p>
</div>

<br/>

<blockquote style="border-left: 5px solid #FF007A; background-color: #FFF0F8; padding: 20px 25px; border-radius: 6px; font-family: 'Segoe UI', sans-serif; color: #333; font-size: 1.1em; box-shadow: 0 4px 10px rgba(255, 0, 122, 0.1);">
  <strong>"Mathematics is the most beautiful and most powerful creation of the human spirit."</strong><br>
  <em>— Stefan Banach</em>
</blockquote>

<br/>

## ✧ About the Project

<div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.8; color: #2c3e50; font-size: 1.05em;">
  <p>
    <strong>Equation Plotter</strong> is not just a graphing calculator; it is a meticulously crafted, interactive graphical engine designed to bridge the gap between raw algebraic computation and human intuition. Engineered for students, educators, and mathematics enthusiasts, this application provides a visually stunning and perfectly fluid canvas for exploring complex mathematical relationships.
  </p>
  <p>
    Under the hood, the application boasts a heavily customized mathematical parser capable of evaluating fractional powers, deep logarithmic boundaries, and reciprocal trigonometric functions with extreme precision. The rendering engine is equipped with custom <strong>Asymptotic Circuit-Breakers</strong> and <strong>Memory-Optimized Marching Squares</strong> algorithms. This ensures that whether you are investigating the subtle curves of implicit equations, dissecting the topological depths of a 3D surface, or animating dynamic parametric loops, your mathematical exploration remains flawlessly smooth, visually accurate, and beautifully rendered at a buttery 60 frames per second.
  </p>
</div>

<hr style="border: 0; height: 2px; background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(121, 40, 202, 0.8), rgba(0, 0, 0, 0)); margin: 50px 0;">

## ✧ Dimensional Capabilities

<table width="100%" style="border: none; border-collapse: separate; border-spacing: 15px;">
  <tr style="border: none;">
    <td width="33%" valign="top" style="border: none; padding: 25px; background-color: #F8F9FA; border-radius: 12px; box-shadow: 0 10px 20px rgba(0, 201, 255, 0.12); border-top: 4px solid #00C9FF;">
      <h3 style="color: #008B8B; margin-top: 0; font-size: 1.4em;">📐 2D Cartesian Space</h3>
      <p style="font-size: 0.95em; line-height: 1.6; color: #444;">
        The backbone of the rendering engine. It flawlessly parses both <strong>Explicit</strong> <code>y = f(x)</code> and <strong>Implicit</strong> <code>f(x,y) = 0</code> equations.<br><br>
        <strong>Advanced Asymptote Handling:</strong> Traditional plotters draw ugly, false vertical lines when a graph jumps to infinity (like in <code>tan(x)</code> or <code>1/cos(x)</code>). Our custom <em>Monotonicity & Discontinuity Engine</em> detects infinite jumps and step-functions (like <code>ceil(x)</code>) in real-time, cleanly breaking the pen path to render mathematically perfect asymptotes.
      </p>
    </td>
    <td width="33%" valign="top" style="border: none; padding: 25px; background-color: #F8F9FA; border-radius: 12px; box-shadow: 0 10px 20px rgba(255, 0, 122, 0.12); border-top: 4px solid #FF007A;">
      <h3 style="color: #C71585; margin-top: 0; font-size: 1.4em;">🧊 3D Surface Topology</h3>
      <p style="font-size: 0.95em; line-height: 1.6; color: #444;">
        Step beyond the flat coordinate plane. The 3D visualization module allows for the exploration of complex multivariable topologies defined by <code>z = f(x,y)</code>.<br><br>
        <strong>Spatial Navigation:</strong> Features intuitive, mouse-driven spatial camera controls—allowing users to seamlessly rotate, pan, and zoom through mathematical landscapes. Dynamic depth-shading algorithms vividly highlight topological intricacies, differentiating peaks, valleys, and saddle points with stunning visual clarity.
      </p>
    </td>
    <td width="33%" valign="top" style="border: none; padding: 25px; background-color: #F8F9FA; border-radius: 12px; box-shadow: 0 10px 20px rgba(121, 40, 202, 0.12); border-top: 4px solid #7928CA;">
      <h3 style="color: #4B0082; margin-top: 0; font-size: 1.4em;">🌀 Polar & Parametric</h3>
      <p style="font-size: 0.95em; line-height: 1.6; color: #444;">
        Explore the mesmerizing beauty of rotational and time-based mathematics. Generate breathtaking rosettes, Archimedean spirals, and Lissajous curves.<br><br>
        <strong>Kinematic Sweeps:</strong> By defining equations via <code>r = f(θ)</code> or kinematic functions like <code>(x(t), y(t))</code>, the engine seamlessly maps temporal and angular inputs onto the interactive canvas. These curves can be flawlessly layered and compared alongside standard Cartesian equations.
      </p>
    </td>
  </tr>
</table>

<br/>

## ✧ Deep Dive: Core Features

<div style="background-color: #ffffff; padding: 30px; border-left: 6px solid #7928CA; box-shadow: 0 8px 24px rgba(121, 40, 202, 0.15); border-radius: 8px;">

<h3 style="color: #FF007A; font-weight: 800; letter-spacing: 1px; margin-top: 0;">🎛️ Real-Time Parameter Animation</h3>
  <p style="color: #444; font-size: 1.05em; line-height: 1.7;">
    Mathematics is dynamic. Type an equation with undefined variables (e.g., <code>y = sin(a * x)</code> or a morphing circle <code>x^2 + y^2 = r^2</code>) and the engine will instantly recognize them, automatically generating sleek interactive UI sliders. 
    <br>As you drag the slider, the graph morphs and reacts in real-time. You can even press the <b>Play (▶)</b> button to let the application animate the parameter smoothly—effectively turning static algebra into a vivid display of calculus in motion.
  </p>

  <hr style="border: 0; height: 1px; background: #eee; margin: 25px 0;">

<h3 style="color: #7928CA; font-weight: 800; letter-spacing: 1px;">⚡ Double-Buffered Marching Squares Optimization</h3>
  <p style="color: #444; font-size: 1.05em; line-height: 1.7;">
    Implicit equations (where <code>x</code> and <code>y</code> are mixed, like <code>y^2 = 4ax</code>) require the computer to evaluate a massive grid of pixels to find the edges of the curve. Traditionally, this causes graphing calculators to freeze or lag terribly when animating.
    <br><strong>The Solution:</strong> We implemented a highly optimized, memory-efficient 1D array variation of the <i>Marching Squares</i> algorithm. Instead of allocating massive 2D matrices that choke the Java Garbage Collector, our engine recycles two 1D rows. Furthermore, when dragging sliders, the app instantly engages a <b>Hyper-Fast Render Mode</b>, drastically reducing grid density to maintain a flawless 60 FPS update rate, snapping back to ultra-high-definition the millisecond you release the mouse.
  </p>

  <hr style="border: 0; height: 1px; background: #eee; margin: 25px 0;">

<h3 style="color: #00C9FF; font-weight: 800; letter-spacing: 1px;">🎯 Dynamic Point Tracking & Tooltips</h3>
  <p style="color: #444; font-size: 1.05em; line-height: 1.7;">
    Visualize coordinates kinematically. By defining a standalone point formatted as <code>(a, sin(a))</code>, you can tether a visual dot to an existing curve. 
    <br>As you adjust the parameter <code>a</code>, the point dynamically traces along its mathematical path like a roller-coaster on a track. Toggle the <b>Label (Aa)</b> button, and the engine will calculate and render a floating tooltip displaying the point's exact mathematical coordinates updating in real-time.
  </p>

  <hr style="border: 0; height: 1px; background: #eee; margin: 25px 0;">

<h3 style="color: #2D3436; font-weight: 800; letter-spacing: 1px;">🌙 Glass-Morphism UI & Deep Dark Mode</h3>
  <p style="color: #444; font-size: 1.05em; line-height: 1.7;">
    Engineered natively with JavaFX, the application features a floating, draggable equation panel with a modern, semi-transparent <i>glass-morphism</i> aesthetic, ensuring your view of the graph is never obstructed. 
    <br>With a single click, instantly toggle between a pristine Light Mode and a deep, immersive <b>Dark Mode</b>. The rendering engine dynamically recolors the background, minor/major gridlines, coordinate axes, text labels, and plot lines to reduce eye strain during late-night mathematical explorations.
  </p>

</div>

<hr style="border: 0; height: 2px; background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(255, 0, 122, 0.8), rgba(0, 0, 0, 0)); margin: 50px 0;">

## ✧ Installation & Setup

<p style="font-size: 1.05em; color: #444;">Ensure you have <strong>Java 21+</strong> and <strong>Apache Maven</strong> installed on your machine. The project handles all JavaFX graphics modules and <code>exp4j</code> mathematical dependencies automatically via the <code>pom.xml</code>.</p>

<div style="background-color: #282A36; padding: 15px; border-radius: 8px; border-left: 5px solid #50FA7B;">
<pre style="color: #F8F8F2; font-family: 'Fira Code', Consolas, monospace; margin: 0; font-size: 0.95em;">
<span style="color: #6272A4;"># 1. Clone the repository</span>
git clone https://github.com/yourusername/equation-plotter.git

<span style="color: #6272A4;"># 2. Navigate into the project directory</span>
cd equation-plotter

<span style="color: #6272A4;"># 3. Clean and compile the application using Maven</span>
mvn clean compile

<span style="color: #6272A4;"># 4. Launch the Visualization Engine</span>
mvn javafx:run
</pre>
</div>

<br/>

## ✧ Tech Stack & Dependencies
* **Core Language:** Java 21 (Leveraging modern Java performance and memory management)
* **UI & Rendering Framework:** JavaFX (Hardware-accelerated Canvas, Custom Controls, and Responsive Layouts)
* **Math Parsing Engine:** [Exp4j](https://www.objecthunter.net/exp4j/) *(Heavily customized and extended at runtime to support fractional powers for negative bases, robust logarithmic domain boundaries, and custom reciprocal trigonometric functions like sec, csc, and cot).*

<br/>

<div align="center" style="margin-top: 40px; padding: 20px; background: linear-gradient(135deg, rgba(255,0,122,0.05), rgba(121,40,202,0.05)); border-radius: 50px;">
  <p style="font-family: 'Helvetica Neue', sans-serif; color: #666; font-size: 1.1em; margin: 0;">
    Designed and built with <span style="color: #FF007A;">❤️</span> and <strong>Math</strong>.
  </p>
</div>