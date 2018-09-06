import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;
class Agent {

	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		g.drawLine((int)m.getX(), (int)m.getY(), (int)m.getDestinationX(), (int)m.getDestinationY());
	}

	void update(Model m)
	{
		Controller c = m.getController();
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
				break;
			m.setDestination(e.getX(), e.getY());
		}
	}

	public static void main(String[] args) throws Exception
	{
		Controller.playGame();
	}
}
class GameState
{
	public double cost;
	GameState parent;
	float[] state;
	GameState(float c, GameState p)
	{
		this.cost = c;
		this.parent = p;
		this.state = new float[2];
		this.state[0] = p.state[0];
		this.state[1] = p.state[1];
	}
	GameState(Model m)
	{
		this.cost = 0.0;
		this.parent = null;
		this.state = new float[2];
		this.state[0] = m.getX();
		this.state[1] = m.getY();
	}
	GameState(float c, GameState p, int movement)
	{
		this.cost = c;
		this.parent = p;
		this.state = new float[2];
		// TODO: Implement logic of moving veritcally, horizontally, and diagonally.
		this.state[0] = p.state[0] + movement;
		this.state[1] = p.state[1] + movement;
	}
	LinkedList<GameState> getSuccessors(GameState gs)
	{
		GameState temp = gs;
		LinkedList<GameState> results = new LinkedList<GameState>();
		results.add(temp);
		while(temp.parent != null)
		{
			results.add(temp.parent);
			temp = temp.parent;
		}
		return results;
	}
}
class MyPlanner
{
	GameState uniformCostSearch(GameState start, GameState goal, int[] actions)
	{
		CostComparator costComp = new CostComparator();
		StateComparator stateComp = new StateComparator();
		PriorityQueue<GameState> frontier = new PriorityQueue<GameState>(costComp);
		TreeSet<GameState> visited = new TreeSet<GameState>(stateComp);
		start.cost = 0.0;
		start.parent = null;
		visited.add(start);
		frontier.add(start);
		while(frontier.size() > 0)
		{
			GameState s = frontier.poll();
			if(s.state.equals(goal.state))
			{
				// Flush queue after finding the goal.
				return s;
			}
			for(int i = 0; i < actions.length; i++)
			{
				GameState child = transition(s, actions[i]);
				float acost = actionCost(s, actions[i]);
				if(visited.contains(child))
				{
					GameState oldChild = visited.floor(child);
					if(s.cost + acost < oldChild.cost)
					{
						oldChild.cost = s.cost + acost;
						oldChild.parent = s;
					}
				}
				else
				{
					child.cost = s.cost + acost;
					child.parent = s;
					frontier.add(child);
					visited.add(child);
				}
			}
		}
		throw new RuntimeException("There is no such path to the goal");
	}
	GameState transition(GameState prev, int a)
	{
		return null; // TODO: PlaceHolder for now.
	}
	float actionCost(GameState prev, int a)
	{
		return 0.0f; // TODO: PlaceHolder for now.
	}
}
class CostComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		if(a.cost < b.cost)
			return -1;
		else if(a.cost > b.cost)
			return 1;
		return 0;
	}
}
class StateComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		for(int i = 0; i < 2; i++)
		{
			if(a.state[i] < b.state[i])
				return -1;
			else if(a.state[i] > b.state[i])
				return 1;
		}
		return 0;
	}
}
